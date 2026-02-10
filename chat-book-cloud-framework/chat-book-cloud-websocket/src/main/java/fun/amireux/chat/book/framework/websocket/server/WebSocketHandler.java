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

/**
 * 从技术与业务来看：
 * WebSocketHandler 相当于一个event listener，主要负责技术层事件，比如：连接建立成功、接收到消息、连接关闭、连接出错。
 * 因此这里bean只负责技术层事件，不处理业务逻辑。
 */

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
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = getUserIdFromSession(session);
        if (StringUtils.hasText(userId)) {
            log.info("连接建立成功，用户ID：{}，SessionID：{}", userId, session.getId());
            session.getAttributes().put("userId", userId);
            sessionManager.register(userId, session);

            // 通知监听器
            if (connectionListeners != null) {
                for (WebSocketConnectionListener listener : connectionListeners) {
                    try {
                        listener.onConnect(userId, session);
                    } catch (Exception e) {
                        log.error("连接监听器执行异常", e);
                    }
                }
            }
        } else {
            log.warn("连接建立成功，但未识别到用户身份，SessionID：{}", session.getId());
            // 可以在这里选择是否允许匿名连接，或者注册为 session ID
            // sessionManager.register(session.getId(), session);
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.info("接收到文本消息：{}", message.getPayload());
        // 路由时使用 userId (如果存在) 还是 sessionId?
        // 建议优先使用 userId，方便后续业务处理
        String userId = (String) session.getAttributes().get("userId");
        String senderId = StringUtils.hasText(userId) ? userId : session.getId();
        messageRouter.route(senderId, message.getPayload());
    }

    @Override
    protected void handleBinaryMessage(WebSocketSession session, BinaryMessage message) throws Exception {
        log.info("接收到二进制消息：{}", message.getPayload());
        String userId = (String) session.getAttributes().get("userId");
        String senderId = StringUtils.hasText(userId) ? userId : session.getId();
        messageRouter.route(senderId, message.getPayload().toString());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.warn("连接关闭：{}", status.getReason());
        String userId = (String) session.getAttributes().get("userId");
        if (StringUtils.hasText(userId)) {
            sessionManager.remove(userId);

            // 通知监听器
            if (connectionListeners != null) {
                for (WebSocketConnectionListener listener : connectionListeners) {
                    try {
                        listener.onClose(userId, session);
                    } catch (Exception e) {
                        log.error("关闭监听器执行异常", e);
                    }
                }
            }
        }
        // 如果也注册了 sessionId，也要移除
        // sessionManager.remove(session.getId());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("连接出错：{}", exception.getMessage(), exception);
        String userId = (String) session.getAttributes().get("userId");
        if (StringUtils.hasText(userId)) {
            sessionManager.remove(userId);
        }
    }

    private String getUserIdFromSession(WebSocketSession session) {
        // 0. 优先从 Session 属性获取 (由 AuthHandshakeInterceptor 注入)
        String userId = String.valueOf(session.getAttributes().get("userId"));
        if (StringUtils.hasText(userId)) {
            return userId;
        }

        // 1. 尝试从 Query Param 获取 token 或 uid
        URI uri = session.getUri();
        if (uri != null && uri.getQuery() != null) {
            String query = uri.getQuery();
            // 简单解析 token=xxx
            String token = extractParam(query, "token");
            if (StringUtils.hasText(token)) {
                return getUserIdFromToken(token);
            }
        }

        // 2. 尝试从 Header 获取 Authorization
        String authHeader = session.getHandshakeHeaders().getFirst("Authorization");
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return getUserIdFromToken(token);
        }

        // 3. 尝试从 Header 获取 token (有些客户端可能直接传 token)
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
            // 假设 token 里有 id 字段
            return jwtUtil.verifyToken(token).getClaim("id").toString();
        } catch (Exception e) {
            log.warn("Token 解析失败: {}", e.getMessage());
            return null;
        }
    }
}
