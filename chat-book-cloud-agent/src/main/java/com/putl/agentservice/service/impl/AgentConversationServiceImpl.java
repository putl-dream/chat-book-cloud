package com.putl.agentservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.enums.AgentMessageRole;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentConversationService;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentConversationServiceImpl implements AgentConversationService {

    private final AgentSessionMapper agentSessionMapper;
    private final AgentMessageMapper agentMessageMapper;
    private final ArticleAiGateway articleAiGateway;

    public AgentConversationServiceImpl(AgentSessionMapper agentSessionMapper,
                                        AgentMessageMapper agentMessageMapper,
                                        ArticleAiGateway articleAiGateway) {
        this.agentSessionMapper = agentSessionMapper;
        this.agentMessageMapper = agentMessageMapper;
        this.articleAiGateway = articleAiGateway;
    }

    @Override
    public AgentChatResponse chat(AgentChatRequest request) {
        AgentSessionDO session = agentSessionMapper.selectById(request.getSessionId());
        saveMessage(request.getSessionId(), AgentMessageRole.USER, request.getContent());
        List<AgentMessageDO> messages = agentMessageMapper.selectList(Wrappers.<AgentMessageDO>lambdaQuery()
                .eq(AgentMessageDO::getSessionId, request.getSessionId())
                .orderByAsc(AgentMessageDO::getId));
        NotebookSummary notebook = JsonUtil.parseObject(session.getNotebookSummary(), NotebookSummary.class);
        String reply = articleAiGateway.chat(messages, notebook);
        saveMessage(request.getSessionId(), AgentMessageRole.ASSISTANT, reply);
        return AgentChatResponse.builder()
                .reply(reply)
                .build();
    }

    private void saveMessage(Integer sessionId, AgentMessageRole role, String content) {
        agentMessageMapper.insert(AgentMessageDO.builder()
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .tokenInput(0)
                .tokenOutput(0)
                .latencyMs(0)
                .build());
    }
}
