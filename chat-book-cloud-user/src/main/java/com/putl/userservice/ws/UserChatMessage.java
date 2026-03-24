package com.putl.userservice.ws;

import com.putl.userservice.controller.vo.MessageVO;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserChatMessage extends BaseMessage {
    private MessageVO data;
}
