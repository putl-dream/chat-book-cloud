package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.CreateAgentSessionRequest;
import com.putl.agentservice.model.vo.AgentSessionCreateResponse;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;

public interface AgentSessionService {

    AgentSessionCreateResponse createSession(CreateAgentSessionRequest request);

    AgentSessionDetailResponse getSessionDetail(Integer sessionId);
}
