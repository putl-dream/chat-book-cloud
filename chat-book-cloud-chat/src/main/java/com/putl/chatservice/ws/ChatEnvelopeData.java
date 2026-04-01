package com.putl.chatservice.ws;

import lombok.Data;

@Data
public class ChatEnvelopeData {

    private String to;

    private Integer receiverId;

    private String content;

    private String msgType;
}
