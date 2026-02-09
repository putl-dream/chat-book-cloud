package com.putl.userservice.ws;

import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SystemMessage extends BaseMessage {
    // Empty, just for type matching
}
