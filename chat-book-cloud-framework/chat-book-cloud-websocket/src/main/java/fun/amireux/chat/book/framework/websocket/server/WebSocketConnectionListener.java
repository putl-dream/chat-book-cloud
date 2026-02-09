package fun.amireux.chat.book.framework.websocket.server;

import org.springframework.web.socket.WebSocketSession;

public interface WebSocketConnectionListener {
    void onConnect(String userId, WebSocketSession session);
    void onClose(String userId, WebSocketSession session);
}
