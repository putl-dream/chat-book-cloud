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

    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final AgentConversationWindowService agentConversationWindowService;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final AgentChatProperties agentChatProperties;
    private final Executor agentChatStreamExecutor;

    public AgentConversationServiceImpl(AgentMessageMapper agentMessageMapper,
                                        ArticleAiGateway articleAiGateway,
                                        AgentConversationWindowService agentConversationWindowService,
                                        AgentNotebookCacheService agentNotebookCacheService,
                                        AgentChatProperties agentChatProperties,
                                        @Qualifier("agentChatStreamExecutor") Executor agentChatStreamExecutor) {
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.agentConversationWindowService = agentConversationWindowService;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.agentChatProperties = agentChatProperties;
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
            List<AgentMessageDO> recentMessages = agentConversationWindowService.getRecentMessages(request.getSessionId());
            AgentMessageDO userMessage = saveMessage(request.getSessionId(), AgentMessageRole.USER, request.getContent());
            List<AgentMessageDO> messages = agentConversationWindowService.appendMessage(request.getSessionId(), recentMessages, userMessage);
            AiInvocationResult<String> reply = articleAiGateway.chatStream(
                    messages,
                    agentNotebookCacheService.getNotebook(request.getSessionId()),
                    chunk -> sendChunk(emitter, chunk));
            AgentMessageDO assistantMessage = saveMessage(
                    request.getSessionId(),
                    AgentMessageRole.ASSISTANT,
                    reply.getData(),
                    reply.getTokenInput(),
                    reply.getTokenOutput(),
                    reply.getLatencyMs());
            agentConversationWindowService.appendMessage(request.getSessionId(), messages, assistantMessage);
            sendEvent(emitter, "done", donePayload(reply));
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

    private void sendChunk(SseEmitter emitter, String chunk) {
        sendEvent(emitter, "delta", Map.of("content", chunk));
    }

    private Map<String, Object> donePayload(AiInvocationResult<String> reply) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("reply", reply.getData());
        payload.put("tokenInput", reply.getTokenInput());
        payload.put("tokenOutput", reply.getTokenOutput());
        payload.put("latencyMs", reply.getLatencyMs());
        payload.put("model", reply.getModel());
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
