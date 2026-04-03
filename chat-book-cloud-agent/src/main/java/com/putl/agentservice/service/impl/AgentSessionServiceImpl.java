package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.config.AnthropicProperties;
import com.putl.agentservice.enums.AgentSessionStatus;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.CreateAgentSessionRequest;
import com.putl.agentservice.model.vo.AgentSessionCreateResponse;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.agentservice.service.AgentSessionService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentSessionServiceImpl implements AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final AgentNotebookService agentNotebookService;
    private final AgentNotebookCacheService agentNotebookCacheService;
    private final AnthropicProperties anthropicProperties;
    private final ArticleClient articleClient;

    public AgentSessionServiceImpl(AgentSessionMapper agentSessionMapper,
                                   AgentMessageMapper agentMessageMapper,
                                   AgentNotebookService agentNotebookService,
                                   AgentNotebookCacheService agentNotebookCacheService,
                                   AnthropicProperties anthropicProperties,
                                   ArticleClient articleClient) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.agentNotebookService = agentNotebookService;
        this.agentNotebookCacheService = agentNotebookCacheService;
        this.anthropicProperties = anthropicProperties;
        this.articleClient = articleClient;
    }

    @Override
    public AgentSessionCreateResponse createSession(CreateAgentSessionRequest request) {
        NotebookSummary notebook = agentNotebookService.initializeNotebook(request.getTitle());
        AgentSessionDO session = AgentSessionDO.builder()
                .userId(currentUserId())
                .sceneType(request.getSceneType())
                .targetArticleId(request.getTargetArticleId())
                .targetDraftId(request.getTargetDraftId())
                .title(request.getTitle())
                .status(AgentSessionStatus.ACTIVE)
                .notebookSummary(JsonUtil.toJsonString(notebook))
                .model(anthropicProperties.getAnthropic().getModel().getChat())
                .promptVersion("v1")
                .build();
        agentSessionMapper.insert(session);
        agentNotebookCacheService.cacheNotebook(session.getId(), notebook);
        return AgentSessionCreateResponse.builder()
                .sessionId(session.getId())
                .build();
    }

    @Override
    public AgentSessionDetailResponse getSessionDetail(Integer sessionId) {
        AgentSessionDO session = agentSessionMapper.selectById(sessionId);
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, sessionId)
                .orderByAsc(AgentMessageDO::getId));
        DraftDetailDTO draft = null;
        if (session != null && session.getTargetDraftId() != null) {
            CommonResult<DraftDetailDTO> draftResult = articleClient.getDraftDetail(session.getTargetDraftId());
            if (draftResult != null && draftResult.isSuccess()) {
                draft = draftResult.getData();
            }
        }
        return AgentSessionDetailResponse.builder()
                .session(session)
                .messages(messages)
                .draft(draft)
                .build();
    }

    private Integer currentUserId() {
        String userId = UserContext.getUserId();
        return (userId == null || userId.isBlank()) ? 0 : Integer.parseInt(userId);
    }
}
