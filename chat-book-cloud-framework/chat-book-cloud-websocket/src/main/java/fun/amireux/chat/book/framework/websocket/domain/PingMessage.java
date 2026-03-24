package fun.amireux.chat.book.framework.websocket.domain;

public final class PingMessage extends BaseMessage {
    public PingMessage() {
        super("PING");
    }
}
