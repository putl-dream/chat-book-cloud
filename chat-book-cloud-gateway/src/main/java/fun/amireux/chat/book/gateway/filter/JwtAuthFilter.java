package fun.amireux.chat.book.gateway.filter;

import com.alibaba.cloud.commons.lang.StringUtils;
import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import fun.amireux.chat.book.gateway.config.AuthConfiguration;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;

import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    @Autowired
    private ReactiveJwtDecoder jwtDecoder;
    @Autowired
    private AuthConfiguration authConfiguration;

    private List<String> excludePaths = Lists.newArrayList();

    @PostConstruct
    public void init() {
        excludePaths = authConfiguration.getWhiteList();
        log.info("白名单路径：{}", excludePaths);
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // 白名单路径直接放行
        if (excludePaths.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // 获取Token
        String token = exchange.getRequest().getHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        if (StringUtils.isEmpty(token) || !token.startsWith("Bearer ")) {
            return onError(exchange, "Token不存在或格式错误", HttpStatus.UNAUTHORIZED);
        }

        token = token.substring(7);

        // 验证Token
        return jwtDecoder.decode(token)
                .flatMap(jwt -> {
                    // Token验证成功，将用户信息添加到请求头
                    ServerHttpRequest request = exchange.getRequest().mutate()
                            .header("X-User-Name", String.valueOf(jwt.getClaims().get("username")))
                            .header("X-User-Role", jwt.getClaimAsString("role"))
                            .build();

                    return chain.filter(exchange.mutate().request(request).build());
                })
                .onErrorResume(ex -> onError(exchange, "Token验证失败", HttpStatus.UNAUTHORIZED));
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus httpStatus) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(httpStatus);
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");

        String result = "{\"code\":" + httpStatus.value() + ",\"message\":\"" + err + "\"}";
        DataBuffer buffer = response.bufferFactory().wrap(result.getBytes());
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100;
    }
}