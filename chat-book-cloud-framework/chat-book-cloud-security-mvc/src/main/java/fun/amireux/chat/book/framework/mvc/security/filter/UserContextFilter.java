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
import org.springframework.security.core.GrantedAuthority;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

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
        String rolesStr = request.getHeader("X-User-Roles");
        String internalToken = request.getHeader("X-Internal-Token");

        boolean isInternalValid = false;
        if (userId != null && !userId.isEmpty() && internalToken != null) {
             // 验证内部签名：防止绕过网关直接攻击，或伪造 Header
             // 数据拼接：userId + "::" + roles (roles 可能为 null)
             String rawData = userId + "::" + rolesStr;
             if (InternalTokenUtil.verifySignature(rawData, internalToken, internalSecret)) {
                 isInternalValid = true;
             } else {
                 log.warn("Internal signature verification failed for user: {}", userId);
             }
        }

        if (isInternalValid) {
            // 既然网关已经校验并通过，直接设置上下文
            setContext(request, userId, username, rolesStr);
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
                    String parsedRoles = null;
                    if (!jwt.getClaim("roles").isNull()) {
                         parsedRoles = jwt.getClaim("roles").asString();
                    }

                    if (parsedUserId != null) {
                        setContext(request, parsedUserId, parsedUsername, parsedRoles);
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

    private void setContext(HttpServletRequest request, String userId, String username, String rolesStr) {
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
        List<GrantedAuthority> authorities = new ArrayList<>();
        if (rolesStr != null && !rolesStr.isEmpty()) {
            // 假设 roles 是逗号分隔的字符串，例如 "ROLE_USER,ROLE_ADMIN"
            authorities = Arrays.stream(rolesStr.split(","))
                    .filter(role -> role != null && !role.trim().isEmpty())
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
        } else {
            // 如果 roles 为 null，用户身份为匿名 (或默认只有 ROLE_USER，根据需求调整)
            // 这里遵循需求：如果 roles 为 null，用户身份为匿名 (即没有任何权限)
            // 但通常已认证用户至少应该有一个 ROLE_USER，或者这里保持空集合表示无特定角色
        }
        
        // 只有当有角色时才设置 authorities，或者即使用户已登录但无角色也允许访问（取决于 Security 配置）
        // 这里的 userId 作为 principal, null 作为 credentials
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                userId, null, authorities);
        
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
