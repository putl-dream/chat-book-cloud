package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class WebSocketPublisherImpl implements MessagePublisher {

    private final SessionManager sessionManager;

    public WebSocketPublisherImpl(SessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public void sendToUser(String userId, Object message) {
        WebSocketSession session = sessionManager.getSession(userId);
        if (session != null && session.isOpen()) {
            try {
                String payload = BeanUtil.toJsonString(message);
                session.sendMessage(new TextMessage(payload));
            } catch (Exception e) {
                log.error("发送消息时发生错误: {}", e.getMessage(), e);
            }
        }
    }

    @Override
    public void broadcast(Object message) {
        sessionManager.getAllSessions().forEach(s -> {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(BeanUtil.toJsonString(message)));
                } catch (Exception ignored) {
                    log.error("发送消息时发生错误: {}", ignored.getMessage(), ignored);
                }
            }
        });
    }
}
