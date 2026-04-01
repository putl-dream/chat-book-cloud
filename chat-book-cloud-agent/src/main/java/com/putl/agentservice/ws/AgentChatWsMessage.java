package com.putl.agentservice.ws;

import com.putl.agentservice.model.dto.AgentChatRequest;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AgentChatWsMessage extends BaseMessage {

    private AgentChatRequest data;

    public AgentChatWsMessage() {
        super("AGENT_CHAT");
    }
}
