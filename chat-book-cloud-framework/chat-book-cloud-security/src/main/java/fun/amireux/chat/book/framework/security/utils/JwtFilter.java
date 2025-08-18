package fun.amireux.chat.book.framework.security.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Component
public class JwtFilter implements WebFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String token = exchange.getRequest().getHeaders().getFirst("Authorization");
        
//        if (token != null && token.startsWith("Bearer ")) {
//            token = token.substring(7);
//            try {
//                String username = Jwts.parser()
//                        .verifyWith(Keys.hmacShaKeyFor("your-secret-key-1234567890".getBytes()))
//                        .build()
//                        .parseSignedClaims(token)
//                        .getPayload()
//                        .getSubject();
//
//                // 将用户信息存入请求上下文
//                exchange.getAttributes().put("username", username);
//            } catch (Exception e) {
//                return Mono.error(new RuntimeException("Invalid token"));
//            }
//        }
        return chain.filter(exchange);
    }
}