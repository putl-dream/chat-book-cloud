package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.CreateAgentSessionRequest;
import com.putl.agentservice.model.vo.AgentSessionCreateResponse;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;
import com.putl.agentservice.model.vo.AgentSessionPageResponse;

public interface AgentSessionService {

    AgentSessionCreateResponse createSession(CreateAgentSessionRequest request);

    AgentSessionDetailResponse getSessionDetail(Integer sessionId);

    AgentSessionPageResponse getSessionPage(Integer pageNo, Integer pageSize, String keyword);
}
