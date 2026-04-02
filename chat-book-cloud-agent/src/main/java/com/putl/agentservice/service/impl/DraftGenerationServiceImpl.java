package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.DraftGenerateResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.DraftGenerationService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.function.Consumer;

@Service
public class DraftGenerationServiceImpl implements DraftGenerationService {

    private static final String AGENT_DRAFT_GENERATE_START = "AGENT_DRAFT_GENERATE_START";
    private static final String AGENT_DRAFT_GENERATE_STATUS = "AGENT_DRAFT_GENERATE_STATUS";
    private static final String AGENT_DRAFT_GENERATE_DELTA = "AGENT_DRAFT_GENERATE_DELTA";
    private static final String AGENT_DRAFT_GENERATE_DONE = "AGENT_DRAFT_GENERATE_DONE";
    private static final String AGENT_DRAFT_GENERATE_ERROR = "AGENT_DRAFT_GENERATE_ERROR";

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final ArticleClient articleClient;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final MessagePublisher messagePublisher;
    private final Executor agentChatStreamExecutor;

    public DraftGenerationServiceImpl(AgentSessionMapper agentSessionMapper,
                                      AgentMessageMapper agentMessageMapper,
                                      ArticleAiGateway articleAiGateway,
                                      ArticleClient articleClient,
                                      AgentNotebookCacheService agentNotebookCacheService,
                                      MessagePublisher messagePublisher,
                                      @Qualifier("agentChatStreamExecutor") Executor agentChatStreamExecutor) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.articleClient = articleClient;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.messagePublisher = messagePublisher;
        this.agentChatStreamExecutor = agentChatStreamExecutor;
    }

    @Override
    public DraftGenerateResponse generateDraft(GenerateDraftRequest request) {
        return doGenerateDraft(request, null, null);
    }

    @Override
    public void generateDraftByWebSocket(String userId, GenerateDraftRequest request) {
        agentChatStreamExecutor.execute(() -> doGenerateDraftByWebSocket(userId, request));
    }

    private void doGenerateDraftByWebSocket(String userId, GenerateDraftRequest request) {
        try {
            send(userId, AGENT_DRAFT_GENERATE_START, Map.of(
                    "sessionId", request.getSessionId(),
                    "message", "正在整理会话上下文..."
            ));
            send(userId, AGENT_DRAFT_GENERATE_STATUS, Map.of(
                    "sessionId", request.getSessionId(),
                    "message", "正在生成首稿内容..."
            ));
            DraftGenerateResponse response = doGenerateDraft(
                    request,
                    chunk -> send(userId, AGENT_DRAFT_GENERATE_DELTA, Map.of(
                    "sessionId", request.getSessionId(),
                    "chunk", chunk
                    )),
                    () -> send(userId, AGENT_DRAFT_GENERATE_STATUS, Map.of(
                            "sessionId", request.getSessionId(),
                            "message", "正在保存草稿..."
                    )));
            send(userId, AGENT_DRAFT_GENERATE_DONE, Map.of(
                    "sessionId", request.getSessionId(),
                    "draftId", response.getDraftId(),
                    "versionNo", response.getVersionNo(),
                    "title", response.getTitle(),
                    "summary", response.getSummary(),
                    "content", response.getContent()
            ));
        } catch (Exception ex) {
            send(userId, AGENT_DRAFT_GENERATE_ERROR, Map.of(
                    "sessionId", request.getSessionId(),
                    "message", defaultText(ex.getMessage(), "生成草稿失败，请稍后重试")
            ));
        }
    }

    private DraftGenerateResponse doGenerateDraft(GenerateDraftRequest request,
                                                  Consumer<String> chunkConsumer,
                                                  Runnable beforePersistHook) {
        AgentSessionDO session = requireSession(request);
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, request.getSessionId())
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary notebook = agentNotebookCacheService.getNotebook(session.getId());
        AiInvocationResult<ArticleDraftResult> result = articleAiGateway.generateDraft(messages, notebook, chunkConsumer);

        if (beforePersistHook != null) {
            beforePersistHook.run();
        }

        CreateDraftResponse response = createDraft(session, result.getData());
        return DraftGenerateResponse.builder()
                .draftId(response.getDraftId())
                .versionNo(response.getVersionNo())
                .title(result.getData().getTitle())
                .summary(result.getData().getSummary())
                .content(result.getData().getContent())
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

        CreateDraftResponse response = articleClient.createDraft(createDraftRequest).getData();
        agentSessionMapper.updateById(AgentSessionDO.builder()
                .id(session.getId())
                .targetDraftId(response.getDraftId())
                .build());
        return response;
    }

    private void send(String userId, String type, Map<String, Object> payload) {
        if (!StringUtils.hasText(userId)) {
            return;
        }
        messagePublisher.sendToUser(userId, WebSocketResult.of(type, payload));
    }

    private String defaultText(String value, String fallback) {
        if (StringUtils.hasText(value)) {
            return value;
        }
        return fallback;
    }
}
