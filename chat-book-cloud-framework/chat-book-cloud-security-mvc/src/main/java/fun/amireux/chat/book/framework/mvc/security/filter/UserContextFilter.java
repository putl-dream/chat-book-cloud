package fun.amireux.chat.book.framework.mvc.security.filter;

import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.context.UserInfo;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import fun.amireux.chat.book.framework.common.utils.InternalTokenUtil;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 从请求头中提取身份信息并存入上下文的过滤器
 */
@Slf4j
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Value("${authentication.internal-secret:chat-book-cloud-internal-secret-key-2026}")
    private String internalSecret;

    private final JwtUtil jwtUtil;

    public UserContextFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 优先从 Gateway 透传的 Header 获取用户信息
        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");
        String internalToken = request.getHeader("X-Internal-Token");

        boolean isInternalValid = false;
        if (userId != null && !userId.isEmpty() && internalToken != null) {
             // 验证内部签名：防止绕过网关直接攻击，或伪造 Header
             if (InternalTokenUtil.verifySignature(userId, internalToken, internalSecret)) {
                 isInternalValid = true;
             } else {
                 log.warn("Internal signature verification failed for user: {}", userId);
             }
        }

        if (isInternalValid) {
            // 既然网关已经校验并通过，直接设置上下文
            setContext(request, userId, username);
        } else {
            // 2. 降级策略：如果没有透传 Header（如直连微服务测试），则自行解析 Token
            String token = request.getHeader("Authorization");
            if (token == null || token.isEmpty()) {
                token = request.getHeader("token");
            }

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (token != null && !token.isEmpty()) {
                try {
                    DecodedJWT jwt = jwtUtil.verifyToken(token);
                    String parsedUserId = null;
                    if (!jwt.getClaim("id").isNull()) {
                        parsedUserId = String.valueOf(jwt.getClaim("id").asLong());
                        if (parsedUserId == null || "null".equals(parsedUserId)) {
                            parsedUserId = jwt.getClaim("id").asString();
                        }
                    }
                    String parsedUsername = jwt.getClaim("username").asString();

                    if (parsedUserId != null) {
                        setContext(request, parsedUserId, parsedUsername);
                    }
                } catch (Exception e) {
                    log.warn("Token validation failed: {}", e.getMessage());
                }
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理上下文，防止内存泄漏
            UserContext.remove();
            SecurityContextHolder.clearContext();
        }
    }

    private void setContext(HttpServletRequest request, String userId, String username) {
        // 获取真实客户端IP
        String clientIp = getClientIp(request);

        // 1. 设置 UserContext (自定义上下文)
        UserInfo userInfo = UserInfo.builder()
                .userId(userId)
                .username(username)
                .clientIp(clientIp)
                .build();
        UserContext.setUser(userInfo);

        // 2. 设置 SecurityContextHolder (Spring Security 上下文)
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
