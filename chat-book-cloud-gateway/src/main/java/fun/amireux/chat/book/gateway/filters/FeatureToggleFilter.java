package fun.amireux.chat.book.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class FeatureToggleFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        // 1️⃣ 判断是否被下架
//        if (isDisabled(path)) {
//            exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
//            return exchange.getResponse().setComplete();
//        }
        log.info("判断是否需要下架: {}", path);
        // 2️⃣ 放行
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0; // 非常靠前
    }
}

