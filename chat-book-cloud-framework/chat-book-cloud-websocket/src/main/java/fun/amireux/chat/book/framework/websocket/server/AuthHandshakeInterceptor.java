package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.common.utils.InternalTokenUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class AuthHandshakeInterceptor implements HandshakeInterceptor {

    @Value("${authentication.internal-secret:chat-book-cloud-internal-secret-key-2026}")
    private String internalSecret;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // 优先从网关透传的 Internal Header 获取认证信息
        String userId = request.getHeaders().getFirst("X-User-Id");
        String internalToken = request.getHeaders().getFirst("X-Internal-Token");
        String roles = request.getHeaders().getFirst("X-User-Roles");

        if (StringUtils.hasText(userId) && StringUtils.hasText(internalToken)) {
            // 校验网关签名
            String rawData = userId + "::" + roles;
            if (InternalTokenUtil.verifySignature(rawData, internalToken, internalSecret)) {
                log.debug("WebSocket握手：使用网关透传的认证信息 (userId={})", userId);
                attributes.put("userId", userId);
                
                // 处理子协议回显 (防止浏览器断开)
                handleSubProtocol(request, response);
                return true;
            } else {
                log.warn("WebSocket握手：网关内部签名校验失败 (userId={})", userId);
                // 签名校验失败，拒绝握手
                return false;
            }
        }
        
        // 如果没有网关透传信息（例如本地直连测试），则尝试自行解析 Token（保留原有逻辑作为兜底，或直接移除）
        // 既然用户要求利用网关校验结果，这里如果校验失败，理论上应该直接拒绝。
        // 但考虑到开发环境可能直连，保留原 Token 解析逻辑作为 fallback 比较稳健，
        // 或者严格按照架构要求只信任网关。
        // 根据用户指令 "通过 InternalTokenUtil 校验成功后 ... 获取即可"，倾向于强制校验网关信息。
        // 不过，WebSocket 握手时，Sec-WebSocket-Protocol 的回显非常关键。
        // 网关虽然校验了 Token，但它**不会**修改 Sec-WebSocket-Protocol 响应头。
        // 这个响应头必须由最终的 WebSocket Server (即本服务) 在 handshake response 中设置。
        
        // 修正逻辑：
        // 1. 尝试从 Header 拿 X-User-Id 和 X-Internal-Token
        // 2. 校验签名
        // 3. 校验通过 -> 设置 attributes -> 处理 Sec-WebSocket-Protocol 回显 -> return true
        // 4. 校验不通过 -> return false
        
        log.warn("WebSocket握手失败：缺少网关透传的认证头 X-User-Id 或 X-Internal-Token");
        return false;
    }

    private void handleSubProtocol(ServerHttpRequest request, ServerHttpResponse response) {
        // 即使认证通过，如果前端用了子协议（new WebSocket(url, [token])），
        // 后端必须把这个 protocol 原样返回，否则 WebSocket 连接会断开。
        List<String> protocolHeaders = request.getHeaders().get("Sec-WebSocket-Protocol");
        if (protocolHeaders != null && !protocolHeaders.isEmpty()) {
            String protocol = protocolHeaders.get(0);
            if (StringUtils.hasText(protocol)) {
                // 这里的 protocol 通常就是 token
                response.getHeaders().set("Sec-WebSocket-Protocol", protocol);
            }
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {

    }
}

