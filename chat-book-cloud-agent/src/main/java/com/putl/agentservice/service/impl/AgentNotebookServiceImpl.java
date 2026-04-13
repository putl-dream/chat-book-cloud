package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import com.putl.agentservice.service.AgentNotebookService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentNotebookServiceImpl implements AgentNotebookService {

    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;
    private final AgentNotebookCacheService agentNotebookCacheService;

    public AgentNotebookServiceImpl(AgentMessageMapper agentMessageMapper,
                                    ArticleAiGateway articleAiGateway,
                                    AgentNotebookCacheService agentNotebookCacheService) {
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
        this.agentNotebookCacheService = agentNotebookCacheService;
    }

    @Override
    public NotebookSummary initializeNotebook(String title) {
        return NotebookSummary.builder()
                .goal(title)
                .summary("会话刚创建，等待收集主题、论点、知识点和待验证的问题。")
                .build();
    }

    @Override
    public NotebookSummary refreshNotebook(Integer sessionId) {
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, sessionId)
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary currentNotebook = agentNotebookCacheService.getNotebook(sessionId);
        AiInvocationResult<NotebookSummary> refreshed = articleAiGateway.summarizeNotebook(messages, currentNotebook);
        agentNotebookCacheService.saveNotebook(sessionId, refreshed.getData());
        return refreshed.getData();
    }
}
