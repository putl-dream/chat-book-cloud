package fun.amireux.chat.book.framework.websocket.server;

public interface MessageRouter {
    void route(String userId, String rawMessage);
}
