package fun.amireux.chat.book.framework.mvc.security.filter;

import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.context.UserInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
@Component
public class UserContextFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String userId = request.getHeader("X-User-Id");
        String username = request.getHeader("X-User-Name");

        if (userId != null && !userId.isEmpty()) {
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

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清理上下文，防止内存泄漏
            UserContext.remove();
            SecurityContextHolder.clearContext();
        }
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
