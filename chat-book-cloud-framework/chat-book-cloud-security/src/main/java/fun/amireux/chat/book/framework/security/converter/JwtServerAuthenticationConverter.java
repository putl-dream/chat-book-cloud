package fun.amireux.chat.book.framework.security.converter;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Collections;

public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = JwtUtil.parseToken(token);
                String username = claims.getSubject();
                return Mono.just(new UsernamePasswordAuthenticationToken(
                        username,
                        token,
                        Collections.emptyList()
                ));
            } catch (Exception e) {
                return Mono.empty();
            }
        }
        return Mono.empty();
    }
}
