package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookService;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentNotebookServiceImpl implements AgentNotebookService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;

    public AgentNotebookServiceImpl(AgentSessionMapper agentSessionMapper,
                                    AgentMessageMapper agentMessageMapper,
                                    ArticleAiGateway articleAiGateway) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
    }

    @Override
    public NotebookSummary initializeNotebook(String title) {
        return NotebookSummary.builder()
                .goal(title)
                .summary("会话刚创建，等待收集写作目标和上下文。")
                .build();
    }

    @Override
    public NotebookSummary refreshNotebook(Integer sessionId) {
        AgentSessionDO session = agentSessionMapper.selectById(sessionId);
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, sessionId)
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary currentNotebook = JsonUtil.parseObject(session.getNotebookSummary(), NotebookSummary.class);
        AiInvocationResult<NotebookSummary> refreshed = articleAiGateway.summarizeNotebook(messages, currentNotebook);
        agentSessionMapper.updateById(AgentSessionDO.builder()
                .id(sessionId)
                .notebookSummary(JsonUtil.toJsonString(refreshed.getData()))
                .build());
        return refreshed.getData();
    }
}
