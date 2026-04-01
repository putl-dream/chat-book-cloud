package com.putl.agentservice.ws;

import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.service.AgentConversationService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class AgentChatMessageHandler implements MessageHandler<AgentChatWsMessage> {

    private static final String AGENT_CHAT = "AGENT_CHAT";
    private static final String AGENT_CHAT_ERROR = "AGENT_CHAT_ERROR";

    @Resource
    private AgentConversationService agentConversationService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return AGENT_CHAT;
    }

    @Override
    public Class<AgentChatWsMessage> getMessageClass() {
        return AgentChatWsMessage.class;
    }

    @Override
    public void handleMessage(String userId, AgentChatWsMessage message) {
        AgentChatRequest request = message.getData();
        if (request == null || request.getSessionId() == null || request.getSessionId() <= 0) {
            sendError(userId, "会话不存在或已失效");
            return;
        }

        String content = request.getContent() == null ? "" : request.getContent().trim();
        if (!StringUtils.hasText(content)) {
            sendError(userId, "消息内容不能为空");
            return;
        }

        request.setContent(content);
        agentConversationService.chatByWebSocket(userId, request);
    }

    private void sendError(String userId, String message) {
        messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_CHAT_ERROR, Map.of("message", message)));
    }
}
