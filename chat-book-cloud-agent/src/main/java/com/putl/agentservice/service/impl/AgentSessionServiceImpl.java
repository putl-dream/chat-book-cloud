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
import com.putl.agentservice.service.AgentNotebookService;
import com.putl.agentservice.service.AgentSessionService;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentSessionServiceImpl implements AgentSessionService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final AgentNotebookService agentNotebookService;
    private final AnthropicProperties anthropicProperties;

    public AgentSessionServiceImpl(AgentSessionMapper agentSessionMapper,
                                   AgentMessageMapper agentMessageMapper,
                                   AgentNotebookService agentNotebookService,
                                   AnthropicProperties anthropicProperties) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.agentNotebookService = agentNotebookService;
        this.anthropicProperties = anthropicProperties;
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
        return AgentSessionDetailResponse.builder()
                .session(session)
                .messages(messages)
                .build();
    }

    private Integer currentUserId() {
        String userId = UserContext.getUserId();
        return (userId == null || userId.isBlank()) ? 0 : Integer.parseInt(userId);
    }
}
