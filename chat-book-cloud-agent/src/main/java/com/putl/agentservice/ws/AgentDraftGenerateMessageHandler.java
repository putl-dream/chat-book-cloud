package com.putl.agentservice.ws;

import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.service.DraftGenerationService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentDraftGenerateMessageHandler implements MessageHandler<AgentDraftGenerateWsMessage> {

    @Resource
    private DraftGenerationService draftGenerationService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return AgentStreamEventConstants.AGENT_DRAFT_GENERATE;
    }

    @Override
    public Class<AgentDraftGenerateWsMessage> getMessageClass() {
        return AgentDraftGenerateWsMessage.class;
    }

    @Override
    public void handleMessage(String userId, AgentDraftGenerateWsMessage message) {
        GenerateDraftRequest request = message.getData();
        if (request == null || request.getSessionId() == null || request.getSessionId() <= 0) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AgentStreamEventConstants.AGENT_DRAFT_GENERATE_ERROR, Map.of("message", "会话不存在或已失效")));
            return;
        }
        draftGenerationService.generateDraftByWebSocket(userId, request);
    }
}
