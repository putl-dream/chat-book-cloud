package fun.amireux.chat.book.gateway.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Slf4j
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

//        String token = extractToken(exchange.getRequest());
//
//        if (token == null) {
//            return unauthorized(exchange);
//        }

//        exchange.getAttributes().put("identity", identity);
        log.info("认证成功: {}", exchange.getRequest().getPath());

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -50;
    }
}
