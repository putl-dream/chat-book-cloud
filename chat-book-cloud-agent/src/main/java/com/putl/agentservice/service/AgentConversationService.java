package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.vo.AgentChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface AgentConversationService {

    AgentChatResponse chat(AgentChatRequest request);

    SseEmitter chatStream(AgentChatRequest request);
}
