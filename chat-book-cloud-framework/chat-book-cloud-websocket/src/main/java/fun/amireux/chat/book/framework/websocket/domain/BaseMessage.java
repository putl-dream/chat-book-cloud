package fun.amireux.chat.book.framework.websocket.domain;

import lombok.Data;

@Data
public abstract class BaseMessage {
    protected String type;   // 消息类型
    protected String requestId; // 请求唯一标识，用于 ACK 追踪

    public BaseMessage(String type) {
        this.type = type;
    }

    public BaseMessage() {
        this.type = "unknown";
    }
}
