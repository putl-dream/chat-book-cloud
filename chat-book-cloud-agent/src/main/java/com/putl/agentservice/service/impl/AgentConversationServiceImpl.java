package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.service.AgentConversationService;
import com.putl.agentservice.service.AgentConversationWindowService;
import com.putl.agentservice.service.AgentNotebookCacheService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {

    private static final String AGENT_CHAT_DELTA = "AGENT_CHAT_DELTA";
    private static final String AGENT_CHAT_DONE = "AGENT_CHAT_DONE";
    private static final String AGENT_CHAT_ERROR = "AGENT_CHAT_ERROR";

    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final AgentConversationWindowService agentConversationWindowService;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final AgentChatProperties agentChatProperties;
    private final Executor agentChatStreamExecutor;
    private final MessagePublisher messagePublisher;

    public AgentConversationServiceImpl(AgentMessageMapper agentMessageMapper,
                                        ArticleAiGateway articleAiGateway,
                                        AgentConversationWindowService agentConversationWindowService,
                                        AgentNotebookCacheService agentNotebookCacheService,
                                        AgentChatProperties agentChatProperties,
                                        MessagePublisher messagePublisher,
                                        @Qualifier("agentChatStreamExecutor") Executor agentChatStreamExecutor) {
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.agentConversationWindowService = agentConversationWindowService;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.agentChatProperties = agentChatProperties;
        this.messagePublisher = messagePublisher;
        this.agentChatStreamExecutor = agentChatStreamExecutor;
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        List<AgentMessageDO> recentMessages = agentConversationWindowService.getRecentMessages(request.getSessionId());
        AgentMessageDO userMessage = saveMessage(request.getSessionId(), AgentMessageRole.USER, request.getContent());
        List<AgentMessageDO> messages = agentConversationWindowService.appendMessage(request.getSessionId(), recentMessages, userMessage);
        var notebook = agentNotebookCacheService.getNotebook(request.getSessionId());
        AiInvocationResult<String> reply = articleAiGateway.chat(messages, notebook);
        AgentMessageDO assistantMessage = saveMessage(
                request.getSessionId(),
                AgentMessageRole.ASSISTANT,
                reply.getData(),
                reply.getTokenInput(),
                reply.getTokenOutput(),
                reply.getLatencyMs());
        agentConversationWindowService.appendMessage(request.getSessionId(), messages, assistantMessage);
        return AgentChatResponse.builder()
                .reply(reply.getData())
                .build();
    }

    @Override
    public SseEmitter chatStream(AgentChatRequest request) {
        SseEmitter emitter = new SseEmitter(Math.max(1000L, agentChatProperties.getStreamTimeoutMs()));
        agentChatStreamExecutor.execute(() -> doChatStream(request, emitter));
        return emitter;
    }

    @Override
    public void chatByWebSocket(String userId, AgentChatRequest request) {
        agentChatStreamExecutor.execute(() -> doChatWebSocket(userId, request));
    }

    private AgentMessageDO saveMessage(Integer sessionId, AgentMessageRole role, String content) {
        return saveMessage(sessionId, role, content, 0, 0, 0);
    }

    private AgentMessageDO saveMessage(Integer sessionId,
                                       AgentMessageRole role,
                                       String content,
                                       Integer tokenInput,
                                       Integer tokenOutput,
                                       Integer latencyMs) {
        AgentMessageDO message = AgentMessageDO.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .tokenInput(tokenInput)
                .tokenOutput(tokenOutput)
                .latencyMs(latencyMs)
                .build();
        agentMessageMapper.insert(message);
        return message;
    }

    private void doChatStream(AgentChatRequest request, SseEmitter emitter) {
        try {
            sendEvent(emitter, "start", Map.of("sessionId", request.getSessionId()));
            AiInvocationResult<String> reply = executeStreamingChat(request, chunk -> sendChunk(emitter, chunk));
            sendEvent(emitter, "done", donePayload(request, reply));
            emitter.complete();
        } catch (Exception ex) {
            try {
                sendEvent(emitter, "error", Map.of("message", defaultText(ex.getMessage())));
                emitter.complete();
            } catch (Exception ignored) {
                emitter.completeWithError(ex);
            }
        }
    }

    private void doChatWebSocket(String userId, AgentChatRequest request) {
        try {
            AiInvocationResult<String> reply = executeStreamingChat(
                    request,
                    chunk -> messagePublisher.sendToUser(
                            userId,
                            WebSocketResult.of(AGENT_CHAT_DELTA, deltaPayload(request, chunk))));
            messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_CHAT_DONE, donePayload(request, reply)));
        } catch (Exception ex) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_CHAT_ERROR, errorPayload(request, ex)));
        }
    }

    private AiInvocationResult<String> executeStreamingChat(AgentChatRequest request, java.util.function.Consumer<String> chunkConsumer) {
        List<AgentMessageDO> recentMessages = agentConversationWindowService.getRecentMessages(request.getSessionId());
        AgentMessageDO userMessage = saveMessage(request.getSessionId(), AgentMessageRole.USER, request.getContent());
        List<AgentMessageDO> messages = agentConversationWindowService.appendMessage(request.getSessionId(), recentMessages, userMessage);
        AiInvocationResult<String> reply = articleAiGateway.chatStream(
                messages,
                agentNotebookCacheService.getNotebook(request.getSessionId()),
                chunkConsumer);
        AgentMessageDO assistantMessage = saveMessage(
                request.getSessionId(),
                AgentMessageRole.ASSISTANT,
                reply.getData(),
                reply.getTokenInput(),
                reply.getTokenOutput(),
                reply.getLatencyMs());
        agentConversationWindowService.appendMessage(request.getSessionId(), messages, assistantMessage);
        return reply;
    }

    private void sendChunk(SseEmitter emitter, String chunk) {
        sendEvent(emitter, "delta", Map.of("content", chunk));
    }

    private Map<String, Object> deltaPayload(AgentChatRequest request, String chunk) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", request.getSessionId());
        payload.put("content", chunk);
        return payload;
    }

    private Map<String, Object> donePayload(AgentChatRequest request, AiInvocationResult<String> reply) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", request.getSessionId());
        payload.put("reply", reply.getData());
        payload.put("tokenInput", reply.getTokenInput());
        payload.put("tokenOutput", reply.getTokenOutput());
        payload.put("latencyMs", reply.getLatencyMs());
        payload.put("model", reply.getModel());
        return payload;
    }

    private Map<String, Object> errorPayload(AgentChatRequest request, Exception ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", request.getSessionId());
        payload.put("message", defaultText(ex.getMessage()));
        return payload;
    }

    private void sendEvent(SseEmitter emitter, String eventName, Object payload) {
        try {
            emitter.send(SseEmitter.event().name(eventName).data(payload));
        } catch (IOException ex) {
            throw new IllegalStateException("SSE 发送失败", ex);
        }
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }
}
