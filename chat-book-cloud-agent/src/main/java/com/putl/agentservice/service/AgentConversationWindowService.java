package com.putl.agentservice.service;

import com.putl.agentservice.mapper.entity.AgentMessageDO;

import java.util.List;

public interface AgentConversationWindowService {

    List<AgentMessageDO> getRecentMessages(Integer sessionId);

    List<AgentMessageDO> appendMessage(Integer sessionId, List<AgentMessageDO> currentWindow, AgentMessageDO message);
}
