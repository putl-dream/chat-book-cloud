package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.vo.AgentChatResponse;

public interface AgentConversationService {

    AgentChatResponse chat(AgentChatRequest request);
}
