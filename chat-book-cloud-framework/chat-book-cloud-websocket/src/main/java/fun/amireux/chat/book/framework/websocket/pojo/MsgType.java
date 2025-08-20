package fun.amireux.chat.book.framework.websocket.pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
    @AllArgsConstructor
    public enum MsgType {
        CHAT, LEAVE, JOIN
    }