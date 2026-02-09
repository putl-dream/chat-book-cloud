package fun.amireux.chat.book.framework.websocket.domain;

import lombok.Data;

@Data
public abstract class BaseMessage {
    protected String type;   // 消息类型

    public BaseMessage(String type) {
        this.type = type;
    }

    public BaseMessage() {
        this.type = "unknown";
    }
}
