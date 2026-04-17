package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.client.engine.StructuredChatOutputFormat;
import com.putl.agentservice.client.engine.StreamingControl;
import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.constants.AgentMessageTypeConstants;
import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.dto.InteractionAnswerRequest;
import com.putl.agentservice.model.dto.InteractionResponseRequest;
import com.putl.agentservice.model.vo.AgentAssistantMessage;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.AgentChatMessageVO;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.model.vo.SceneDecision;
import com.putl.agentservice.service.AgentConversationService;
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.agentservice.service.AgentConversationWindowService;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class AgentConversationServiceImpl implements AgentConversationService {

    private final AgentMessageMapper agentMessageMapper;
    private final AgentSessionMapper agentSessionMapper;
    private final ArticleAiGateway articleAiGateway;
    private final AgentConversationWindowService agentConversationWindowService;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final AgentNotebookService agentNotebookService;
    private final AgentSceneRouter agentSceneRouter;
    private final ArticleClient articleClient;
    private final AgentChatProperties agentChatProperties;
    private final Executor agentChatStreamExecutor;
    private final MessagePublisher messagePublisher;

    public AgentConversationServiceImpl(AgentMessageMapper agentMessageMapper,
                                        AgentSessionMapper agentSessionMapper,
                                        ArticleAiGateway articleAiGateway,
                                        AgentConversationWindowService agentConversationWindowService,
                                        AgentNotebookCacheService agentNotebookCacheService,
                                        AgentNotebookService agentNotebookService,
                                        AgentSceneRouter agentSceneRouter,
                                        ArticleClient articleClient,
                                        AgentChatProperties agentChatProperties,
                                        MessagePublisher messagePublisher,
                                        @Qualifier("agentChatStreamExecutor") Executor agentChatStreamExecutor) {
        this.agentMessageMapper = agentMessageMapper;
        this.agentSessionMapper = agentSessionMapper;
        this.articleAiGateway = articleAiGateway;
        this.agentConversationWindowService = agentConversationWindowService;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.agentNotebookService = agentNotebookService;
        this.agentSceneRouter = agentSceneRouter;
        this.articleClient = articleClient;
        this.agentChatProperties = agentChatProperties;
        this.messagePublisher = messagePublisher;
        this.agentChatStreamExecutor = agentChatStreamExecutor;
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        ChatExecutionResult result = executeChat(request, ignored -> { }, StreamingControl.noop(), new StreamingChatPreviewState());
        return AgentChatResponse.builder()
                .reply(result.assistantMessage().getContent())
                .message(toMessageVO(result.assistantMessage()))
                .currentScene(result.sceneDecision().getCurrentScene())
                .nextScene(result.sceneDecision().getNextScene())
                .switchReason(result.sceneDecision().getSwitchReason())
                .assistantAction(result.sceneDecision().getAssistantAction())
                .draftReadiness(result.sceneDecision().getDraftReadiness())
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
        return executeChat(request, ignored -> { }, StreamingControl.noop(), new StreamingChatPreviewState());
    }

    private ChatExecutionResult executeChat(AgentChatRequest request,
                                            java.util.function.Consumer<String> previewConsumer,
                                            StreamingControl streamingControl,
                                            StreamingChatPreviewState previewState) {
        PreparedUserMessage preparedUserMessage = prepareUserMessage(request);
        AgentSessionDO session = requireSession(request == null ? null : request.getSessionId());
        NotebookSummary currentNotebook = agentNotebookCacheService.getNotebook(session.getId());
        DraftDetailDTO draftDetail = loadDraftDetail(session);
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
        SceneDecision routedScene = agentSceneRouter.route(session, messages, currentNotebook, draftDetail != null);
        AiInvocationResult<AgentAssistantMessage> aiReply = articleAiGateway.chat(
                messages,
                currentNotebook,
                routedScene.getCurrentScene(),
                draftDetail == null ? null : draftDetail.getTitle(),
                draftDetail == null ? null : draftDetail.getSummary(),
                draftDetail == null ? null : draftDetail.getContent(),
                chunk -> handleStreamingChunk(chunk, previewConsumer, previewState),
                streamingControl);
        AssistantMessageResolution resolution = reconcileAssistantMessage(aiReply.getData(), previewState);
        AgentAssistantMessage assistant = resolution.message();
        aiReply.setData(assistant);
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
        NotebookSummary stabilizedNotebook = safelyRefreshNotebook(
                request.getSessionId(),
                currentNotebook,
                routedScene,
                draftDetail != null);
        SceneDecision finalizedScene = agentSceneRouter.finalizeDecision(routedScene, stabilizedNotebook, draftDetail != null);
        StreamingChatSummary streamSummary = buildStreamSummary(previewState, resolution);
        logChatStreamTelemetry(request == null ? null : request.getSessionId(), aiReply, assistant, streamSummary);
        return new ChatExecutionResult(aiReply, assistantMessage, finalizedScene, streamSummary);
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
            Map<String, Object> startPayload = new LinkedHashMap<>();
            startPayload.put("sessionId", request == null ? null : request.getSessionId());
            sendEvent(emitter, "start", startPayload);
            StreamingChatPreviewState previewState = new StreamingChatPreviewState();
            ChatExecutionResult result = executeChat(request, preview ->
                    sendEvent(emitter, "delta", payload(request == null ? null : request.getSessionId(), "content", preview)),
                    StreamingControl.noop(),
                    previewState);
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
            Integer sessionId = request == null ? null : request.getSessionId();
            sendStart(userId, sessionId);
            StreamingChatPreviewState previewState = new StreamingChatPreviewState();
            ChatExecutionResult result = executeChat(request, preview ->
                    sendDelta(userId, sessionId, preview), StreamingControl.noop(), previewState);
            messagePublisher.sendToUser(userId, WebSocketResult.of(AgentStreamEventConstants.AGENT_CHAT_DONE, donePayload(request, result)));
        } catch (Exception ex) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AgentStreamEventConstants.AGENT_CHAT_ERROR, errorPayload(request, ex)));
        }
    }

    private void handleStreamingChunk(String chunk,
                                      java.util.function.Consumer<String> previewConsumer,
                                      StreamingChatPreviewState previewState) {
        if (previewState == null || chunk == null || chunk.isEmpty()) {
            return;
        }
        previewState.rawBuffer.append(chunk);
        String rawBuffer = previewState.rawBuffer.toString();
        String previewContent = StructuredChatOutputFormat.extractPreview(rawBuffer);
        if (StringUtils.hasText(previewContent)) {
            previewState.previewMode = "tagged_preview";
            publishPreviewDelta(previewContent, previewConsumer, previewState);
            return;
        }
        String compatibilityPreview = StructuredMessageStreamPreviewExtractor.extractContent(rawBuffer);
        if (StringUtils.hasText(compatibilityPreview)) {
            previewState.previewMode = "json_content_fallback";
            publishPreviewDelta(compatibilityPreview, previewConsumer, previewState);
        }
    }

    private void sendStart(String userId, Integer sessionId) {
        messagePublisher.sendToUser(userId, WebSocketResult.of(
                AgentStreamEventConstants.AGENT_CHAT_START,
                payload(sessionId, "message", "正在思考...", "renderHint", "text_preview")));
    }

    private void sendDelta(String userId, Integer sessionId, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        messagePublisher.sendToUser(userId, WebSocketResult.of(
                AgentStreamEventConstants.AGENT_CHAT_DELTA,
                payload(sessionId, "content", content)));
    }

    private Map<String, Object> donePayload(AgentChatRequest request, ChatExecutionResult result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", request.getSessionId());
        payload.put("reply", result.assistantMessage().getContent());
        payload.put("message", toMessageVO(result.assistantMessage()));
        payload.put("currentScene", result.sceneDecision().getCurrentScene());
        payload.put("nextScene", result.sceneDecision().getNextScene());
        payload.put("switchReason", defaultText(result.sceneDecision().getSwitchReason()));
        payload.put("assistantAction", result.sceneDecision().getAssistantAction());
        payload.put("draftReadiness", result.sceneDecision().getDraftReadiness());
        payload.put("tokenInput", result.aiReply().getTokenInput());
        payload.put("tokenOutput", result.aiReply().getTokenOutput());
        payload.put("latencyMs", result.aiReply().getLatencyMs());
        payload.put("model", result.aiReply().getModel());
        payload.put("streamPreview", result.streamSummary().previewText());
        payload.put("streamMeta", streamMeta(result.streamSummary()));
        return payload;
    }

    private Map<String, Object> errorPayload(AgentChatRequest request, Exception ex) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", request == null ? null : request.getSessionId());
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

    private Map<String, Object> payload(Integer sessionId, Object... values) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId);
        if (values == null) {
            return payload;
        }
        for (int index = 0; index + 1 < values.length; index += 2) {
            payload.put(String.valueOf(values[index]), values[index + 1]);
        }
        return payload;
    }

    private String defaultText(String value) {
        return value == null ? "" : value;
    }

    private AgentSessionDO requireSession(Integer sessionId) {
        if (sessionId == null || sessionId <= 0) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        AgentSessionDO session = agentSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        return session;
    }

    private DraftDetailDTO loadDraftDetail(AgentSessionDO session) {
        if (session == null || session.getTargetDraftId() == null || session.getTargetDraftId() <= 0) {
            return null;
        }
        CommonResult<DraftDetailDTO> draftResult = articleClient.getDraftDetail(session.getTargetDraftId());
        if (draftResult == null || !draftResult.isSuccess()) {
            return null;
        }
        return draftResult.getData();
    }

    private NotebookSummary safelyRefreshNotebook(Integer sessionId,
                                                  NotebookSummary fallbackNotebook,
                                                  SceneDecision sceneDecision,
                                                  boolean hasDraftContext) {
        try {
            NotebookSummary refreshedNotebook = agentNotebookService.refreshNotebook(sessionId);
            NotebookSummary stabilizedNotebook = agentSceneRouter.applyDecision(refreshedNotebook, sceneDecision, hasDraftContext);
            agentNotebookCacheService.saveNotebook(sessionId, stabilizedNotebook);
            return stabilizedNotebook;
        } catch (Exception ex) {
            log.warn("Failed to refresh notebook after chat. sessionId={}, reason={}", sessionId, ex.getMessage(), ex);
            NotebookSummary stabilizedNotebook = agentSceneRouter.applyDecision(fallbackNotebook, sceneDecision, hasDraftContext);
            agentNotebookCacheService.saveNotebook(sessionId, stabilizedNotebook);
            return stabilizedNotebook;
        }
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

    private AssistantMessageResolution reconcileAssistantMessage(AgentAssistantMessage message,
                                                                StreamingChatPreviewState previewState) {
        AgentAssistantMessage normalized = normalizeAssistantMessage(message);
        String previewText = previewState == null ? "" : defaultText(previewState.lastPreview).trim();
        if (!StringUtils.hasText(previewText)) {
            return new AssistantMessageResolution(normalized, false, false);
        }

        String messageType = defaultText(normalized.getMessageType()).trim().toLowerCase(Locale.ROOT);
        String finalContent = defaultText(normalized.getContent()).trim();
        boolean previewMismatch = StringUtils.hasText(finalContent) && !previewText.equals(finalContent);

        if (!StringUtils.hasText(finalContent)) {
            return new AssistantMessageResolution(
                    AgentAssistantMessage.builder()
                            .messageType(StringUtils.hasText(messageType) ? messageType : AgentMessageTypeConstants.TEXT)
                            .content(previewText)
                            .payload(normalized.getPayload())
                            .build(),
                    true,
                    true);
        }

        if (previewText.startsWith(finalContent) && previewText.length() > finalContent.length()) {
            return new AssistantMessageResolution(
                    AgentAssistantMessage.builder()
                            .messageType(StringUtils.hasText(messageType) ? messageType : AgentMessageTypeConstants.TEXT)
                            .content(previewText)
                            .payload(normalized.getPayload())
                            .build(),
                    true,
                    true);
        }

        return new AssistantMessageResolution(normalized, previewMismatch, false);
    }

    private void publishPreviewDelta(String previewContent,
                                     java.util.function.Consumer<String> previewConsumer,
                                     StreamingChatPreviewState previewState) {
        if (!StringUtils.hasText(previewContent) || previewContent.equals(previewState.lastPreview)) {
            return;
        }

        String delta = previewContent.startsWith(previewState.lastPreview)
                ? previewContent.substring(previewState.lastPreview.length())
                : previewContent;
        previewState.lastPreview = previewContent;
        if (!StringUtils.hasText(delta)) {
            return;
        }
        previewState.deltaCount += 1;
        if (previewState.firstDeltaLatencyMs == null) {
            previewState.firstDeltaLatencyMs = Math.max(0L, System.currentTimeMillis() - previewState.startedAtMs);
        }
        previewConsumer.accept(delta);
    }

    private StreamingChatSummary buildStreamSummary(StreamingChatPreviewState previewState,
                                                   AssistantMessageResolution resolution) {
        return new StreamingChatSummary(
                previewState == null ? "" : defaultText(previewState.lastPreview),
                previewState == null ? "none" : defaultText(previewState.previewMode),
                previewState == null || previewState.firstDeltaLatencyMs == null
                        ? null
                        : Math.toIntExact(Math.min(Integer.MAX_VALUE, previewState.firstDeltaLatencyMs)),
                previewState == null ? 0 : previewState.deltaCount,
                resolution.previewMismatch(),
                resolution.previewFallbackApplied());
    }

    private Map<String, Object> streamMeta(StreamingChatSummary streamSummary) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("previewMode", defaultText(streamSummary.previewMode()));
        payload.put("deltaCount", streamSummary.deltaCount());
        payload.put("firstDeltaLatencyMs", streamSummary.firstDeltaLatencyMs());
        payload.put("previewMismatch", streamSummary.previewMismatch());
        payload.put("previewFallbackApplied", streamSummary.previewFallbackApplied());
        return payload;
    }

    private void logChatStreamTelemetry(Integer sessionId,
                                        AiInvocationResult<AgentAssistantMessage> aiReply,
                                        AgentAssistantMessage assistant,
                                        StreamingChatSummary streamSummary) {
        log.info("Agent chat stream completed. sessionId={}, previewMode={}, deltaCount={}, firstDeltaLatencyMs={}, completionLatencyMs={}, previewChars={}, previewMismatch={}, previewFallbackApplied={}, messageType={}, model={}",
                sessionId,
                defaultText(streamSummary.previewMode()),
                streamSummary.deltaCount(),
                streamSummary.firstDeltaLatencyMs(),
                aiReply == null ? null : aiReply.getLatencyMs(),
                defaultText(streamSummary.previewText()).length(),
                streamSummary.previewMismatch(),
                streamSummary.previewFallbackApplied(),
                assistant == null ? null : assistant.getMessageType(),
                aiReply == null ? null : aiReply.getModel());
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
                                       AgentMessageDO assistantMessage,
                                       SceneDecision sceneDecision,
                                       StreamingChatSummary streamSummary) {
    }

    private record AssistantMessageResolution(AgentAssistantMessage message,
                                              boolean previewMismatch,
                                              boolean previewFallbackApplied) {
    }

    private record StreamingChatSummary(String previewText,
                                        String previewMode,
                                        Integer firstDeltaLatencyMs,
                                        int deltaCount,
                                        boolean previewMismatch,
                                        boolean previewFallbackApplied) {
    }

    private static final class StreamingChatPreviewState {

        private final long startedAtMs = System.currentTimeMillis();
        private final StringBuilder rawBuffer = new StringBuilder();
        private String lastPreview = "";
        private String previewMode = "none";
        private Long firstDeltaLatencyMs;
        private int deltaCount;
    }
}
