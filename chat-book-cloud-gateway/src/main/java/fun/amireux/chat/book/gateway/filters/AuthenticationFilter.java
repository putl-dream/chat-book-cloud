package fun.amireux.chat.book.gateway.filters;

import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
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

        List<AuthenticationProperties.AuthenticationRule> rules = authenticationProperties.getRules();
        if (CollectionUtils.isEmpty(rules)) {
            return chain.filter(exchange);
        }

        for (AuthenticationProperties.AuthenticationRule rule : rules) {
            if (antPathMatcher.match(rule.getPattern(), path)) {
                log.info("[ 身份认证 ]: 功能 {}，接口 {}", rule.getName(), path);

                String token = extractToken(exchange.getRequest());
                if (token == null) {
                    throw new AuthenticationException("未找到认证信息");
                }
                String identity = jwtUtil.verifyToken(token).getClaim("username").asString();

                exchange.getAttributes().put("identity", identity);
                log.info("认证成功: {}", exchange.getRequest().getPath());

                return chain.filter(exchange);
            }
        }

        return chain.filter(exchange);
    }

    private String extractToken(ServerHttpRequest request) {
        return request.getHeaders().getFirst("Authorization");
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
