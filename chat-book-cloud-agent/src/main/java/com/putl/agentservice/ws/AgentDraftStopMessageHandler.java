package com.putl.agentservice.ws;

import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.service.DraftGenerationService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class AgentDraftStopMessageHandler implements MessageHandler<AgentDraftStopWsMessage> {

    private static final String AGENT_DRAFT_GENERATE_STOP = "AGENT_DRAFT_GENERATE_STOP";
    private static final String AGENT_DRAFT_GENERATE_ERROR = "AGENT_DRAFT_GENERATE_ERROR";

    @Resource
    private DraftGenerationService draftGenerationService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return AGENT_DRAFT_GENERATE_STOP;
    }

    @Override
    public Class<AgentDraftStopWsMessage> getMessageClass() {
        return AgentDraftStopWsMessage.class;
    }

    @Override
    public void handleMessage(String userId, AgentDraftStopWsMessage message) {
        GenerateDraftRequest request = message.getData();
        if (request == null || request.getSessionId() == null || request.getSessionId() <= 0) {
            messagePublisher.sendToUser(userId, WebSocketResult.of(AGENT_DRAFT_GENERATE_ERROR, Map.of("message", "会话不存在或已失效")));
            return;
        }
        draftGenerationService.cancelDraftGenerationByWebSocket(userId, request);
    }
}
