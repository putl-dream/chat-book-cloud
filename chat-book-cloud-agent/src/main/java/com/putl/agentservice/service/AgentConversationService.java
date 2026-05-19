package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.model.vo.AgentChatResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Agent对话服务接口
 * <p>提供与AI Agent进行对话交互的核心功能，支持同步、SSE流式和WebSocket三种通信方式</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public interface AgentConversationService {

    /**
     * 同步对话
     *
     * @param request 对话请求参数
     * @return 对话响应结果
     */
    AgentChatResponse chat(AgentChatRequest request);

    /**
     * SSE流式对话
     *
     * @param request 对话请求参数
     * @return SSE发射器，用于推送流式响应
     */
    SseEmitter chatStream(AgentChatRequest request);

    /**
     * WebSocket对话
     *
     * @param userId 用户ID
     * @param request 对话请求参数
     */
    void chatByWebSocket(String userId, AgentChatRequest request);

    /**
     * 取消WebSocket对话
     *
     * @param userId 用户ID
     * @param request 对话请求参数
     */
    void cancelChatByWebSocket(String userId, AgentChatRequest request);
}
