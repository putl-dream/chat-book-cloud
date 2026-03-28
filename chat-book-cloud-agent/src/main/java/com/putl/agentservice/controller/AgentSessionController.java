package com.putl.agentservice.controller;

import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.dto.CreateAgentSessionRequest;
import com.putl.agentservice.model.vo.AgentChatResponse;
import com.putl.agentservice.model.vo.AgentSessionCreateResponse;
import com.putl.agentservice.model.vo.AgentSessionDetailResponse;
import com.putl.agentservice.service.AgentConversationService;
import com.putl.agentservice.service.AgentSessionService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Tag(name = "Agent 会话")
@RestController
@RequestMapping("/agent/session")
public class AgentSessionController {

    private final AgentSessionService agentSessionService;
    private final AgentConversationService agentConversationService;

    public AgentSessionController(AgentSessionService agentSessionService,
                                  AgentConversationService agentConversationService) {
        this.agentSessionService = agentSessionService;
        this.agentConversationService = agentConversationService;
    }

    @Operation(summary = "创建会话")
    @PostMapping("/create")
    public CommonResult<AgentSessionCreateResponse> create(@RequestBody CreateAgentSessionRequest request) {
        return CommonResult.success(agentSessionService.createSession(request));
    }

    @Operation(summary = "发送消息并获取回复")
    @PostMapping("/chat")
    public CommonResult<AgentChatResponse> chat(@RequestBody AgentChatRequest request) {
        return CommonResult.success(agentConversationService.chat(request));
    }

    @Operation(summary = "发送消息并流式获取回复")
    @PostMapping(value = "/chat/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter chatStream(@RequestBody AgentChatRequest request) {
        return agentConversationService.chatStream(request);
    }

    @Operation(summary = "获取会话详情")
    @GetMapping("/detail")
    public CommonResult<AgentSessionDetailResponse> detail(@RequestParam("sessionId") Integer sessionId) {
        return CommonResult.success(agentSessionService.getSessionDetail(sessionId));
    }
}
