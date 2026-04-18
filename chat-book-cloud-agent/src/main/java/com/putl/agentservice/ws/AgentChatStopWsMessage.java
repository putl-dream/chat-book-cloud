package com.putl.agentservice.ws;

import com.putl.agentservice.constants.AgentStreamEventConstants;
import com.putl.agentservice.model.dto.AgentChatRequest;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentChatStopWsMessage extends BaseMessage {

    private AgentChatRequest data;

    public AgentChatStopWsMessage() {
        super(AgentStreamEventConstants.MESSAGE_STOP);
    }
}
