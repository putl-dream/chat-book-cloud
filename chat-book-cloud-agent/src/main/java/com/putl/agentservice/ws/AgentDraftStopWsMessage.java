package com.putl.agentservice.ws;

import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentDraftStopWsMessage extends BaseMessage {

    private GenerateDraftRequest data;

    public AgentDraftStopWsMessage() {
        super(AgentStreamEventConstants.AGENT_DRAFT_GENERATE_STOP);
    }
}
