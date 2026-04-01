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
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DraftGenerationServiceImpl implements DraftGenerationService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final ArticleClient articleClient;
    private final AgentNotebookCacheService agentNotebookCacheService;

    public DraftGenerationServiceImpl(AgentSessionMapper agentSessionMapper,
                                      AgentMessageMapper agentMessageMapper,
                                      ArticleAiGateway articleAiGateway,
                                      ArticleClient articleClient,
                                      AgentNotebookCacheService agentNotebookCacheService) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.articleClient = articleClient;
        this.agentNotebookCacheService = agentNotebookCacheService;
    }

    @Override
    public DraftGenerateResponse generateDraft(GenerateDraftRequest request) {
        AgentSessionDO session = agentSessionMapper.selectById(request.getSessionId());
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, request.getSessionId())
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary notebook = agentNotebookCacheService.getNotebook(session.getId());
        AiInvocationResult<ArticleDraftResult> result = articleAiGateway.generateDraft(messages, notebook);

        CreateDraftRequest createDraftRequest = new CreateDraftRequest();
        createDraftRequest.setUserId(session.getUserId());
        createDraftRequest.setSourceSessionId(session.getId());
        createDraftRequest.setTitle(result.getData().getTitle());
        createDraftRequest.setSummary(result.getData().getSummary());
        createDraftRequest.setContent(result.getData().getContent());
        createDraftRequest.setSourceType("CREATE");
        createDraftRequest.setInstruction("Generate draft from agent session");

        CreateDraftResponse response = articleClient.createDraft(createDraftRequest).getData();
        agentSessionMapper.updateById(AgentSessionDO.builder()
                .id(session.getId())
                .targetDraftId(response.getDraftId())
                .build());

        return DraftGenerateResponse.builder()
                .draftId(response.getDraftId())
                .versionNo(response.getVersionNo())
                .title(result.getData().getTitle())
                .summary(result.getData().getSummary())
                .content(result.getData().getContent())
                .build();
    }
}
