package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.dto.InteractionAnswerRequest;
import com.putl.agentservice.model.dto.InteractionResponseRequest;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.AgentChatMessageVO;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.service.AgentConversationService;
import com.putl.agentservice.service.AgentConversationWindowService;
import com.putl.agentservice.service.AgentNotebookCacheService;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executor;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {

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
        ChatExecutionResult result = executeChat(request);
        return AgentChatResponse.builder()
                .reply(result.aiReply().getData().getContent())
                .message(toMessageVO(result.assistantMessage()))
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

    private ChatExecutionResult executeChat(AgentChatRequest request) {
        PreparedUserMessage preparedUserMessage = prepareUserMessage(request);
        List<AgentMessageDO> recentMessages = agentConversationWindowService.getRecentMessages(request.getSessionId());
        AgentMessageDO userMessage = saveMessage(
                request.getSessionId(),
                AgentMessageRole.USER,
                AgentMessageTypeConstants.TEXT,
                preparedUserMessage.content(),
                preparedUserMessage.payload(),
                0,
                0,
                0);
        List<AgentMessageDO> messages = agentConversationWindowService.appendMessage(request.getSessionId(), recentMessages, userMessage);
        AiInvocationResult<AgentAssistantMessage> aiReply = articleAiGateway.chat(messages, agentNotebookCacheService.getNotebook(request.getSessionId()));
        AgentAssistantMessage assistant = normalizeAssistantMessage(aiReply.getData());
        AgentMessageDO assistantMessage = saveMessage(
                request.getSessionId(),
                AgentMessageRole.ASSISTANT,
                assistant.getMessageType(),
                assistant.getContent(),
                JsonUtil.toJsonString(assistant.getPayload()),
                aiReply.getTokenInput(),
                aiReply.getTokenOutput(),
                aiReply.getLatencyMs());
        agentConversationWindowService.appendMessage(request.getSessionId(), messages, assistantMessage);
        return new ChatExecutionResult(aiReply, assistantMessage);
    }

    private AgentMessageDO saveMessage(Integer sessionId,
                                       AgentMessageRole role,
                                       String messageType,
                                       String content,
                                       String payload,
                                       Integer tokenInput,
                                       Integer tokenOutput,
                                       Integer latencyMs) {
        AgentMessageDO message = AgentMessageDO.builder()
                .sessionId(sessionId)
                .role(role)
                .messageType(defaultText(messageType))
                .content(content)
                .payload(payload)
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
            ChatExecutionResult result = executeChat(request);
            sendEvent(emitter, "done", donePayload(request, result));
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
            ChatExecutionResult result = executeChat(request);
            messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_CHAT_DONE, donePayload(request, result)));
        } catch (Exception ex) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_CHAT_ERROR, errorPayload(request, ex)));
        }
    }

    private Map<String, Object> donePayload(AgentChatRequest request, ChatExecutionResult result) {
        return Map.of(
                "sessionId", request.getSessionId(),
                "reply", result.aiReply().getData().getContent(),
                "message", toMessageVO(result.assistantMessage()),
                "tokenInput", result.aiReply().getTokenInput(),
                "tokenOutput", result.aiReply().getTokenOutput(),
                "latencyMs", result.aiReply().getLatencyMs(),
                "model", result.aiReply().getModel());
    }

    private Map<String, Object> errorPayload(AgentChatRequest request, Exception ex) {
        return Map.of(
                "sessionId", request.getSessionId(),
                "message", defaultText(ex.getMessage()));
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

    private AgentAssistantMessage normalizeAssistantMessage(AgentAssistantMessage message) {
        if (message == null) {
            return AgentAssistantMessage.builder()
                    .messageType(AgentMessageTypeConstants.TEXT)
                    .content("")
                    .payload(null)
                    .build();
        }
        String messageType = defaultText(message.getMessageType()).trim().toLowerCase(Locale.ROOT);
        if (!StringUtils.hasText(messageType)) {
            messageType = AgentMessageTypeConstants.TEXT;
        }
        if (!AgentMessageTypeConstants.INTERACTIVE_FORM.equals(messageType)) {
            return AgentAssistantMessage.builder()
                    .messageType(AgentMessageTypeConstants.TEXT)
                    .content(defaultText(message.getContent()).trim())
                    .payload(null)
                    .build();
        }
        return AgentAssistantMessage.builder()
                .messageType(AgentMessageTypeConstants.INTERACTIVE_FORM)
                .content(defaultText(message.getContent()).trim())
                .payload(message.getPayload())
                .build();
    }

    private PreparedUserMessage prepareUserMessage(AgentChatRequest request) {
        InteractionResponseRequest interactionResponse = request.getInteractionResponse();
        if (interactionResponse != null && !CollectionUtils.isEmpty(interactionResponse.getAnswers())) {
            return new PreparedUserMessage(
                    buildInteractionResponseContent(interactionResponse),
                    JsonUtil.toJsonString(Map.of("interactionResponse", interactionResponse)));
        }

        String content = defaultText(request.getContent()).trim();
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
        return new PreparedUserMessage(content, null);
    }

    private String buildInteractionResponseContent(InteractionResponseRequest response) {
        StringBuilder builder = new StringBuilder("[STRUCTURED_FORM_RESPONSE]");
        if (StringUtils.hasText(response.getTitle())) {
            builder.append('\n').append("表单标题: ").append(response.getTitle().trim());
        }
        if (StringUtils.hasText(response.getDescription())) {
            builder.append('\n').append("表单说明: ").append(response.getDescription().trim());
        }
        if (StringUtils.hasText(response.getFormId())) {
            builder.append('\n').append("表单ID: ").append(response.getFormId().trim());
        }
        for (InteractionAnswerRequest answer : response.getAnswers()) {
            if (answer == null) {
                continue;
            }
            String label = StringUtils.hasText(answer.getQuestionLabel())
                    ? answer.getQuestionLabel().trim()
                    : defaultText(answer.getQuestionId()).trim();
            String renderedValue = renderAnswerValue(answer.getValue());
            if (!StringUtils.hasText(label) || !StringUtils.hasText(renderedValue)) {
                continue;
            }
            builder.append('\n').append("- ").append(label).append(": ").append(renderedValue);
        }
        return builder.toString();
    }

    private String renderAnswerValue(Object value) {
        if (value == null) {
            return "";
        }
        if (value instanceof Iterable<?> iterable) {
            StringJoiner joiner = new StringJoiner("、");
            for (Object item : iterable) {
                if (item != null && StringUtils.hasText(item.toString())) {
                    joiner.add(item.toString().trim());
                }
            }
            return joiner.toString();
        }
        if (value.getClass().isArray()) {
            Object[] array = (Object[]) value;
            StringJoiner joiner = new StringJoiner("、");
            for (Object item : array) {
                if (item != null && StringUtils.hasText(item.toString())) {
                    joiner.add(item.toString().trim());
                }
            }
            return joiner.toString();
        }
        if (value instanceof Map<?, ?> mapValue) {
            return defaultText(JsonUtil.toJsonString(mapValue));
        }
        return value.toString().trim();
    }

    private AgentChatMessageVO toMessageVO(AgentMessageDO message) {
        return AgentChatMessageVO.builder()
                .id(message.getId())
                .role(message.getRole() == null ? null : message.getRole().name())
                .messageType(defaultText(message.getMessageType()))
                .content(defaultText(message.getContent()))
                .payload(parsePayload(message.getPayload()))
                .createTime(message.getCreateTime())
                .build();
    }

    private Object parsePayload(String payload) {
        if (!StringUtils.hasText(payload)) {
            return null;
        }
        Object parsed = JsonUtil.parseTree(payload);
        return parsed == null ? payload : parsed;
    }

    private record PreparedUserMessage(String content, String payload) {
    }

    private record ChatExecutionResult(AiInvocationResult<AgentAssistantMessage> aiReply,
                                       AgentMessageDO assistantMessage) {
    }
}
