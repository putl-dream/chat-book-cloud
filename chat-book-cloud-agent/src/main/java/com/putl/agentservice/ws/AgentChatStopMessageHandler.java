package com.putl.agentservice.ws;

import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.model.dto.AgentChatRequest;
import com.putl.agentservice.service.AgentConversationService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentChatStopMessageHandler implements MessageHandler<AgentChatStopWsMessage> {

    @Resource
    private AgentConversationService agentConversationService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return AgentStreamEventConstants.MESSAGE_STOP;
    }

    @Override
    public Class<AgentChatStopWsMessage> getMessageClass() {
        return AgentChatStopWsMessage.class;
    }

    @Override
    public void handleMessage(String userId, AgentChatStopWsMessage message) {
        AgentChatRequest request = message.getData();
        if (request == null || request.getSessionId() == null || request.getSessionId() <= 0) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AgentStreamEventConstants.MESSAGE_FAILED, Map.of("message", "会话不存在或已失效")));
            return;
        }
        agentConversationService.cancelChatByWebSocket(userId, request);
    }
}
