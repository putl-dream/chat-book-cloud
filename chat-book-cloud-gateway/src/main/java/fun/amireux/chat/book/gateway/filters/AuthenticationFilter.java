package fun.amireux.chat.book.gateway.filters;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import fun.amireux.chat.book.framework.common.utils.InternalTokenUtil;
import fun.amireux.chat.book.gateway.config.AuthenticationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;
    private final AuthenticationProperties authenticationProperties;
    private final AntPathMatcher antPathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        String token = extractToken(request);
        String userId = null;
        String username = null;
        String roles = null;
        String tokenType = null;

        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        if (token != null) {
            try {
                com.auth0.jwt.interfaces.DecodedJWT decodedJWT = jwtUtil.verifyToken(token);

                // Extract token type (null for pre-existing tokens without a type claim)
                try {
                    tokenType = decodedJWT.getClaim("type").asString();
                } catch (Exception ignored) {
                    // pre-existing tokens have no type claim
                }

                if (!decodedJWT.getClaim("id").isNull()) {
                    userId = String.valueOf(decodedJWT.getClaim("id").asLong());
                    if (userId == null || "null".equals(userId)) {
                        userId = decodedJWT.getClaim("id").asString();
                    }
                }
                username = decodedJWT.getClaim("username").asString();
                if (!decodedJWT.getClaim("roles").isNull()) {
                     roles = decodedJWT.getClaim("roles").asString();
                }
            } catch (fun.amireux.chat.book.framework.common.exceptions.AuthenticationException e) {
                // Re-throw auth exceptions as 401
                throw e;
            } catch (Exception e) {
                log.error("Token verification failed: {}", e.getMessage());
            }
        }

        List<AuthenticationProperties.AuthenticationRule> rules = authenticationProperties.getRules();
        boolean isMandatory = false;

        if (!CollectionUtils.isEmpty(rules)) {
            for (AuthenticationProperties.AuthenticationRule rule : rules) {
                if (antPathMatcher.match(rule.getPattern(), path)) {
                    isMandatory = true;
                    log.info("[ 身份认证 ]: 功能 {}，接口 {}", rule.getName(), path);
                    break;
                }
            }
        }

        // Reject refresh tokens on protected routes — treat as unauthenticated
        if (JwtUtil.TOKEN_TYPE_REFRESH.equals(tokenType) && isMandatory) {
            log.warn("Refresh token used on protected route: {}", path);
            throw new AuthenticationException("Refresh token cannot be used to access protected resources");
        }

        if (isMandatory && userId == null) {
            throw new AuthenticationException("未找到有效认证信息");
        }

        // 解析设备信息
        String userAgent = request.getHeaders().getFirst("User-Agent");
        String deviceInfo = DeviceUtil.getDeviceInfo(userAgent);
        log.info("[ 身份认证 ]: 用户 {}，设备 {}", userId, deviceInfo);

        // 无论是否必选，只要有身份信息，就传递给下游
        if (userId != null) {
            // 生成内部签名，防止篡改
            // 数据拼接：userId + "::" + roles (roles 可能为 null)
            String rawData = userId + "::" + roles;
            String internalToken = InternalTokenUtil.generateSignature(rawData, authenticationProperties.getInternalSecret());

            ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .header("X-User-Name", username)
                    .header("X-Internal-Token", internalToken)
                    .header("X-Device-Info", deviceInfo);

            if (roles != null) {
                builder.header("X-User-Roles", roles);
            }

            return chain.filter(exchange.mutate().request(builder.build()).build());
        }

        // 即使没有身份信息，也传递设备信息
        ServerHttpRequest.Builder builder = exchange.getRequest().mutate()
                .header("X-Device-Info", deviceInfo);

        return chain.filter(exchange.mutate().request(builder.build()).build());
    }

    private String extractToken(ServerHttpRequest request) {
        String token = request.getHeaders().getFirst("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeaders().getFirst("token");
        }
        if (token == null || token.isEmpty()) {
            token = request.getQueryParams().getFirst("token");
        }
        if (token == null || token.isEmpty()) {
            List<String> protocols = request.getHeaders().get("Sec-WebSocket-Protocol");
            if (!CollectionUtils.isEmpty(protocols)) {
                for (String protocol : protocols) {
                    if (protocol != null && !protocol.isEmpty()) {
                        String[] parts = protocol.split(",");
                        for (String part : parts) {
                            String trimmed = part.trim();
                            if (trimmed.length() > 20) {
                                token = trimmed;
                                break;
                            }
                        }
                    }
                    if (token != null) break;
                }
            }
        }
        return token;
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
