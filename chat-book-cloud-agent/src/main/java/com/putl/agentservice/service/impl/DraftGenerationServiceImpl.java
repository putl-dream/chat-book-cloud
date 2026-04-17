package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.client.engine.StreamingCancelledException;
import com.putl.agentservice.client.engine.StreamingControl;
import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.DraftGenerateResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.model.vo.SceneDecision;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.DraftGenerationService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Slf4j
@Service
public class DraftGenerationServiceImpl implements DraftGenerationService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final ArticleClient articleClient;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final MessagePublisher messagePublisher;
    private final ActiveDraftGenerationRegistry activeDraftGenerationRegistry;
    private final AgentSceneRouter agentSceneRouter;
    private final Executor agentChatStreamExecutor;

    public DraftGenerationServiceImpl(AgentSessionMapper agentSessionMapper,
                                      AgentMessageMapper agentMessageMapper,
                                      ArticleAiGateway articleAiGateway,
                                      ArticleClient articleClient,
                                      AgentNotebookCacheService agentNotebookCacheService,
                                      MessagePublisher messagePublisher,
                                      ActiveDraftGenerationRegistry activeDraftGenerationRegistry,
                                      AgentSceneRouter agentSceneRouter,
                                      @Qualifier("agentChatStreamExecutor") Executor agentChatStreamExecutor) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.articleClient = articleClient;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.messagePublisher = messagePublisher;
        this.activeDraftGenerationRegistry = activeDraftGenerationRegistry;
        this.agentSceneRouter = agentSceneRouter;
        this.agentChatStreamExecutor = agentChatStreamExecutor;
    }

    @Override
    public DraftGenerateResponse generateDraft(GenerateDraftRequest request) {
        return doGenerateDraft(request, StreamingControl.noop(), null, null);
    }

    @Override
    public void generateDraftByWebSocket(String userId, GenerateDraftRequest request) {
        ActiveDraftGenerationRegistry.DraftGenerationHandle handle = activeDraftGenerationRegistry.register(
                userId,
                request == null ? null : request.getSessionId());
        agentChatStreamExecutor.execute(() -> doGenerateDraftByWebSocket(handle, request));
    }

    @Override
    public void cancelDraftGenerationByWebSocket(String userId, GenerateDraftRequest request) {
        Integer sessionId = request == null ? null : request.getSessionId();
        ActiveDraftGenerationRegistry.DraftGenerationHandle handle = activeDraftGenerationRegistry.cancel(
                userId,
                sessionId,
                "User requested stop");
        log.info("Agent draft generation stop requested. sessionId={}, userId={}, activeTaskFound={}",
                sessionId, userId, handle != null);
        send(userId, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_STOPPED, payload(sessionId,
                "message", "已停止生成，你可以直接接管正文"));
    }

    private void doGenerateDraftByWebSocket(ActiveDraftGenerationRegistry.DraftGenerationHandle handle,
                                            GenerateDraftRequest request) {
        Integer sessionId = request == null ? null : request.getSessionId();
        Thread executionThread = Thread.currentThread();
        handle.onCancel(executionThread::interrupt);
        try {
            log.info("Starting agent draft generation. sessionId={}, userId={}", sessionId, handle.getUserId());
            sendIfActive(handle, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_START, payload(sessionId, "message", "正在整理会话上下文..."));
            sendIfActive(handle, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_STATUS, payload(sessionId, "message", "正在生成首稿内容..."));
            DraftGenerateResponse response = doGenerateDraft(
                    request,
                    handle,
                    chunk -> {
                        handle.throwIfCancelled();
                        sendIfActive(handle, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_DELTA, payload(sessionId, "chunk", chunk));
                    },
                    () -> {
                        handle.throwIfCancelled();
                        sendIfActive(handle, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_STATUS, payload(sessionId, "message", "正在保存草稿..."));
                    });
            handle.throwIfCancelled();
            sendIfActive(handle, AgentStreamEventConstants.AGENT_DRAFT_GENERATE_DONE, payload(sessionId,
                    "draftId", response.getDraftId(),
                    "versionNo", response.getVersionNo(),
                    "title", response.getTitle(),
                    "summary", response.getSummary(),
                    "content", response.getContent(),
                    "currentScene", response.getCurrentScene(),
                    "nextScene", response.getNextScene(),
                    "assistantAction", response.getAssistantAction(),
                    "draftReadiness", response.getDraftReadiness()));
            log.info("Completed agent draft generation. sessionId={}, userId={}, draftId={}, versionNo={}",
                    sessionId, handle.getUserId(), response.getDraftId(), response.getVersionNo());
        } catch (StreamingCancelledException ex) {
            log.info("Agent draft generation cancelled. sessionId={}, userId={}, reason={}",
                    sessionId, handle.getUserId(), defaultText(handle.getCancelReason(), "cancelled"));
        } catch (Exception ex) {
            if (handle.isCancelled()) {
                log.info("Agent draft generation finished after cancellation. sessionId={}, userId={}, reason={}",
                        sessionId, handle.getUserId(), defaultText(handle.getCancelReason(), ex.getMessage()));
                return;
            }
            log.error("Agent draft generation failed. sessionId={}, userId={}, reason={}",
                    sessionId, handle.getUserId(), ex.getMessage(), ex);
            send(handle.getUserId(), AgentStreamEventConstants.AGENT_DRAFT_GENERATE_ERROR, payload(sessionId,
                    "message", defaultText(ex.getMessage(), "生成草稿失败，请稍后重试")));
        } finally {
            activeDraftGenerationRegistry.complete(handle);
            Thread.interrupted();
        }
    }

    private DraftGenerateResponse doGenerateDraft(GenerateDraftRequest request,
                                                  StreamingControl streamingControl,
                                                  Consumer<String> chunkConsumer,
                                                  Runnable beforePersistHook) {
        StreamingControl safeStreamingControl = streamingControl == null ? StreamingControl.noop() : streamingControl;
        safeStreamingControl.throwIfCancelled();
        AgentSessionDO session = requireSession(request);
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, request.getSessionId())
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary notebook = agentNotebookCacheService.getNotebook(session.getId());
        AiInvocationResult<ArticleDraftResult> result = articleAiGateway.generateDraft(
                messages,
                notebook,
                chunkConsumer,
                safeStreamingControl);
        safeStreamingControl.throwIfCancelled();
        ArticleDraftResult draftResult = requireDraftResult(result, request.getSessionId());

        if (beforePersistHook != null) {
            safeStreamingControl.throwIfCancelled();
            beforePersistHook.run();
        }

        safeStreamingControl.throwIfCancelled();
        CreateDraftResponse response = createDraft(session, draftResult);
        SceneDecision sceneDecision = updateNotebookAfterDraftGeneration(session.getId());
        return DraftGenerateResponse.builder()
                .draftId(response.getDraftId())
                .versionNo(response.getVersionNo())
                .title(draftResult.getTitle())
                .summary(draftResult.getSummary())
                .content(draftResult.getContent())
                .currentScene(sceneDecision.getCurrentScene())
                .nextScene(sceneDecision.getNextScene())
                .assistantAction(sceneDecision.getAssistantAction())
                .draftReadiness(sceneDecision.getDraftReadiness())
                .build();
    }

    private AgentSessionDO requireSession(GenerateDraftRequest request) {
        if (request == null || request.getSessionId() == null || request.getSessionId() <= 0) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        AgentSessionDO session = agentSessionMapper.selectById(request.getSessionId());
        if (session == null) {
            throw new IllegalArgumentException("会话不存在或已失效");
        }
        return session;
    }

    private CreateDraftResponse createDraft(AgentSessionDO session, ArticleDraftResult result) {
        CreateDraftRequest createDraftRequest = new CreateDraftRequest();
        createDraftRequest.setUserId(session.getUserId());
        createDraftRequest.setSourceSessionId(session.getId());
        createDraftRequest.setTitle(result.getTitle());
        createDraftRequest.setSummary(result.getSummary());
        createDraftRequest.setContent(result.getContent());
        createDraftRequest.setSourceType("CREATE");
        createDraftRequest.setInstruction("Generate draft from agent session");

        CommonResult<CreateDraftResponse> createDraftResult = articleClient.createDraft(createDraftRequest);
        if (createDraftResult == null) {
            throw new IllegalStateException("文章草稿服务未返回结果");
        }
        if (!createDraftResult.isSuccess() || createDraftResult.getData() == null) {
            throw new IllegalStateException(defaultText(createDraftResult.getMsg(), "文章草稿保存失败"));
        }
        CreateDraftResponse response = createDraftResult.getData();
        if (response.getDraftId() == null || response.getVersionNo() == null) {
            throw new IllegalStateException("文章草稿服务返回了不完整的保存结果");
        }
        agentSessionMapper.updateById(AgentSessionDO.builder()
                .id(session.getId())
                .targetDraftId(response.getDraftId())
                .build());
        return response;
    }

    private ArticleDraftResult requireDraftResult(AiInvocationResult<ArticleDraftResult> result, Integer sessionId) {
        if (result == null || result.getData() == null) {
            throw new IllegalStateException("AI 未返回可用的草稿内容");
        }
        ArticleDraftResult draftResult = result.getData();
        if (!StringUtils.hasText(draftResult.getTitle())
                && !StringUtils.hasText(draftResult.getSummary())
                && !StringUtils.hasText(draftResult.getContent())) {
            throw new IllegalStateException("AI 返回的草稿内容为空");
        }
        log.info("AI draft generated. sessionId={}, model={}, tokenInput={}, tokenOutput={}, latencyMs={}",
                sessionId,
                defaultText(result.getModel(), "unknown"),
                Objects.requireNonNullElse(result.getTokenInput(), 0),
                Objects.requireNonNullElse(result.getTokenOutput(), 0),
                Objects.requireNonNullElse(result.getLatencyMs(), 0));
        return draftResult;
    }

    private SceneDecision updateNotebookAfterDraftGeneration(Integer sessionId) {
        try {
            NotebookSummary currentNotebook = agentNotebookCacheService.getNotebook(sessionId);
            SceneDecision sceneDecision = agentSceneRouter.draftGeneratedDecision(currentNotebook);
            NotebookSummary updatedNotebook = agentSceneRouter.applyDecision(currentNotebook, sceneDecision, true);
            agentNotebookCacheService.saveNotebook(sessionId, updatedNotebook);
            return agentSceneRouter.finalizeDecision(sceneDecision, updatedNotebook, true);
        } catch (Exception ex) {
            log.warn("Failed to update notebook after draft generation. sessionId={}, reason={}", sessionId, ex.getMessage(), ex);
            return agentSceneRouter.draftGeneratedDecision(NotebookSummary.builder().build());
        }
    }

    private void send(String userId, String type, Map<String, Object> payload) {
        if (!StringUtils.hasText(userId)) {
            log.warn("Skipping WebSocket send because userId is blank. type={}, payload={}", type, payload);
            return;
        }
        messagePublisher.sendToUser(userId, WebSocketResult.of(type, payload));
    }

    private void sendIfActive(ActiveDraftGenerationRegistry.DraftGenerationHandle handle,
                              String type,
                              Map<String, Object> payload) {
        if (handle == null || handle.isCancelled() || !activeDraftGenerationRegistry.isCurrent(handle)) {
            return;
        }
        send(handle.getUserId(), type, payload);
    }

    private Map<String, Object> payload(Integer sessionId, Object... keyValues) {
        if (keyValues.length % 2 != 0) {
            throw new IllegalArgumentException("payload keyValues length must be even");
        }
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sessionId", sessionId);
        for (int i = 0; i < keyValues.length; i += 2) {
            payload.put(String.valueOf(keyValues[i]), keyValues[i + 1]);
        }
        return payload;
    }

    private String defaultText(String value, String fallback) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return fallback;
    }
}
