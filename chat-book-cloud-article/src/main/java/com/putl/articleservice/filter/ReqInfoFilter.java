package com.putl.articleservice.filter;

import com.putl.articleservice.common.ReqInfoContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 请求信息过滤器
 * 从请求头中提取用户上下文信息并设置到 ReqInfoContext 中
 *
 * @since 2026-01-31
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ReqInfoFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_CLIENT_IP = "X-Client-IP";
    private static final String HEADER_DEVICE = "X-Device";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 从请求头中获取用户信息（由网关传递）
            String userIdStr = request.getHeader(HEADER_USER_ID);
            String clientIp = request.getHeader(HEADER_CLIENT_IP);
            String device = request.getHeader(HEADER_DEVICE);

            // 如果请求头中没有，尝试从其他地方获取
            if (userIdStr == null || userIdStr.isEmpty()) {
                // 尝试从 attribute 中获取（网关可能已经设置）
                Object userIdObj = request.getAttribute("userId");
                if (userIdObj != null) {
                    userIdStr = String.valueOf(userIdObj);
                }
            }

            // 获取真实客户端IP
            if (clientIp == null || clientIp.isEmpty()) {
                clientIp = getClientIp(request);
            }

            // 如果有用户ID，设置到上下文中
            if (userIdStr != null && !userIdStr.isEmpty()) {
                try {
                    int userId = Integer.parseInt(userIdStr);
                    ReqInfoContext.ReqInfo reqInfo = new ReqInfoContext.ReqInfo(
                            clientIp,
                            request.getRequestURI(),
                            null,  // payload 不需要在此处解析
                            device,
                            userId
                    );
                    ReqInfoContext.addReqInfo(reqInfo);
                    log.debug("[ReqInfoFilter] 设置用户上下文: userId={}, uri={}", userId, request.getRequestURI());
                } catch (NumberFormatException e) {
                    log.warn("[ReqInfoFilter] 无效的 userId 格式: {}", userIdStr);
                }
            } else {
                log.debug("[ReqInfoFilter] 未获取到用户ID，跳过上下文设置，uri={}", request.getRequestURI());
            }

            filterChain.doFilter(request, response);

        } finally {
            // 清理 ThreadLocal，防止内存泄漏
            ReqInfoContext.clear();
        }
    }

    /**
     * 获取客户端真实IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果有多个代理，取第一个IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
