package fun.amireux.chat.book.framework.websocket.server;

public interface MessagePublisher {
    void sendToUser(String userId, Object message);
    void broadcast(Object message);
}
