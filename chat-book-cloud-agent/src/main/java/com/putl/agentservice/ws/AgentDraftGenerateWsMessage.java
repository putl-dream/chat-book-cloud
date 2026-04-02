package com.putl.agentservice.ws;

import com.putl.agentservice.model.dto.GenerateDraftRequest;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDraftGenerateWsMessage extends BaseMessage {

    private GenerateDraftRequest data;

    public AgentDraftGenerateWsMessage() {
        super("AGENT_DRAFT_GENERATE");
    }
}
