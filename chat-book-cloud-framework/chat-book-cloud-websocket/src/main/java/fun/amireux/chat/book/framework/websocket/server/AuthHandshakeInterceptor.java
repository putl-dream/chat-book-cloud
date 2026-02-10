package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Resource
    private JwtUtil jwtUtil;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 1. 尝试从 Sec-WebSocket-Protocol (子协议) 获取 token
        // 前端: new WebSocket(url, [token]) 会将 token 放入此 Header
        List<String> protocolHeaders = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocolHeaders != null && !protocolHeaders.isEmpty()) {
            // 通常取第一个，且假设 token 就是协议名
            String token = protocolHeaders.get(0); 
            if (StringUtils.hasText(token)) {
                // 验证 token
                String userId = getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    // 关键: 必须将 token 作为选中的子协议返回，否则浏览器会断开连接
                    response.getHeaders().set("Sec-WebSocket-Protocol", token);
                    return true;
                }
            }
        }

        // 2. 尝试从 Query Param 获取 token
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String token = servletRequest.getServletRequest().getParameter("token");
            if (StringUtils.hasText(token)) {
                 String userId = getUserIdFromToken(token);
                 if (userId != null) {
                     attributes.put("userId", userId);
                     return true;
                 }
            }
        }

        // 3. 尝试从 Authorization Header 获取
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String authHeader = authHeaders.get(0);
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String userId = getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    return true;
                }
            }
        }
        
        // 4. 尝试从 token Header 获取
        List<String> tokenHeaders = request.getHeaders().get("token");
        if (tokenHeaders != null && !tokenHeaders.isEmpty()) {
            String token = tokenHeaders.get(0);
            if (StringUtils.hasText(token)) {
                String userId = getUserIdFromToken(token);
                if (userId != null) {
                    attributes.put("userId", userId);
                    return true;
                }
            }
        }

        // 如果未认证，允许握手继续（作为匿名用户），但如果是 Protocol 方式且未通过，浏览器可能会报错（因为没返回 Protocol 头）
        // 这里返回 true 允许连接
        return true; 
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }

    private String getUserIdFromToken(String token) {
        try {
            return jwtUtil.verifyToken(token).getClaim("id").toString();
        } catch (Exception e) {
            log.warn("WebSocket握手认证失败: {}", e.getMessage());
            return null;
        }
    }
}
