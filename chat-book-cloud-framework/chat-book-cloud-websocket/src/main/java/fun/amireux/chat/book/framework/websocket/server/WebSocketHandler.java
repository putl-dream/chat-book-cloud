package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.net.URI;
import java.util.List;

@Slf4j
@Component
public class WebSocketHandler extends AbstractWebSocketHandler {

    @Resource
    private SessionManager sessionManager;

    @Resource
    private MessageRouter messageRouter;

    @Resource
    private JwtUtil jwtUtil;

    private final List<WebSocketConnectionListener> connectionListeners;

    public WebSocketHandler(List<WebSocketConnectionListener> connectionListeners) {
        this.connectionListeners = connectionListeners;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        String senderId = StringUtils.hasText(userId) ? userId : session.getId();

        // Keep a stable key for routing/reply even when userId is unavailable.
        session.getAttributes().put("senderId", senderId);
        if (StringUtils.hasText(userId)) {
            session.getAttributes().put("userId", userId);
            log.info("WebSocket connected, userId={}, sessionId={}", userId, session.getId());
        } else {
            log.warn("WebSocket connected without user identity, sessionId={}", session.getId());
        }

        sessionManager.register(senderId, session);

        if (connectionListeners != null) {
            for (WebSocketConnectionListener listener : connectionListeners) {
                try {
                    listener.onConnect(senderId, session);
                } catch (Exception e) {
                    log.error("WebSocket connection listener error", e);
                }
            }
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        log.info("Received text message: {}", message.getPayload());
        String senderId = getSenderId(session);
        messageRouter.route(senderId, message.getPayload());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) {
        log.info("Received binary message: {}", message.getPayload());
        String senderId = getSenderId(session);
        messageRouter.route(senderId, message.getPayload().toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.warn("WebSocket closed: {}", status.getReason());
        String senderId = getSenderId(session);
        sessionManager.remove(senderId);

        if (connectionListeners != null) {
            for (WebSocketConnectionListener listener : connectionListeners) {
                try {
                    listener.onClose(senderId, session);
                } catch (Exception e) {
                    log.error("WebSocket close listener error", e);
                }
            }
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("WebSocket transport error: {}", exception.getMessage(), exception);
        String senderId = getSenderId(session);
        sessionManager.remove(senderId);
    }

    private String getSenderId(WebSocketSession session) {
        Object senderIdAttr = session.getAttributes().get("senderId");
        if (senderIdAttr != null) {
            String senderId = String.valueOf(senderIdAttr);
            if (StringUtils.hasText(senderId) && !"null".equalsIgnoreCase(senderId)) {
                return senderId;
            }
        }

        String userId = (String) session.getAttributes().get("userId");
        return StringUtils.hasText(userId) ? userId : session.getId();
    }

    private String getUserIdFromSession(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr != null) {
            String userId = String.valueOf(userIdAttr).trim();
            if (StringUtils.hasText(userId) && !"null".equalsIgnoreCase(userId)) {
                return userId;
            }
        }

        // If the client uses subprotocol to pass token: new WebSocket(url, [token])
        String acceptedProtocol = session.getAcceptedProtocol();
        if (StringUtils.hasText(acceptedProtocol)) {
            String userId = getUserIdFromToken(acceptedProtocol);
            if (StringUtils.hasText(userId)) {
                return userId;
            }
        }

        // Query param token=xxx
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String token = extractParam(uri.getQuery(), "token");
            if (StringUtils.hasText(token)) {
                return getUserIdFromToken(token);
            }
        }

        // Authorization: Bearer xxx
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return getUserIdFromToken(authHeader.substring(7));
        }

        // token: xxx
        String tokenHeader = session.getHandshakeHeaders().getFirst("token");
        if (StringUtils.hasText(tokenHeader)) {
            return getUserIdFromToken(tokenHeader);
        }

        return null;
    }

    private String extractParam(String query, String key) {
        String paramPattern = key + "=";
        int startIdx = query.indexOf(paramPattern);
        if (startIdx >= 0) {
            startIdx += paramPattern.length();
            int endIdx = query.indexOf('&', startIdx);
            return endIdx >= 0 ? query.substring(startIdx, endIdx) : query.substring(startIdx);
        }
        return null;
    }

    private String getUserIdFromToken(String token) {
        try {
            return jwtUtil.verifyToken(token).getClaim("id").toString();
        } catch (Exception e) {
            log.warn("Token parse failed: {}", e.getMessage());
            return null;
        }
    }
}
