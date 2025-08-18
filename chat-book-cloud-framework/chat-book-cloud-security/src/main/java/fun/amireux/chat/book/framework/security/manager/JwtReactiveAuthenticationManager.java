package fun.amireux.chat.book.framework.security.manager;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.getSubject();
            List<SimpleGrantedAuthority> authorities = Arrays.stream(String.valueOf(claims.get("role")).split(","))
                    .map(String::trim)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());
            return Mono.just(new UsernamePasswordAuthenticationToken(
                    username,
                    token,
                    authorities
            ));
        } catch (Exception e) {
            return Mono.empty();
        }
    }
}
