package fun.amireux.chat.book.framework.websocket.server;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class WebSocketSessionManagerImpl implements SessionManager {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void register(String userId, WebSocketSession session) {
        sessions.put(userId, session);
    }

    @Override
    public void remove(String userId) {
        sessions.remove(userId);
    }

    @Override
    public WebSocketSession getSession(String userId) {
        return sessions.get(userId);
    }

    @Override
    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }
}
