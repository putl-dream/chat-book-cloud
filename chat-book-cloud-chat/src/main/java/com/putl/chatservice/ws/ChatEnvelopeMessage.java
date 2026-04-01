package com.putl.chatservice.ws;

import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ChatEnvelopeMessage extends BaseMessage {

    private ChatEnvelopeData data;

    public ChatEnvelopeMessage() {
        super("CHAT");
    }
}
