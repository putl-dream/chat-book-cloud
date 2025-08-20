package fun.amireux.chat.book.framework.flux.security.core;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;

import java.util.List;

public class JwtReactiveAuthenticationManager implements ReactiveAuthenticationManager {

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        String token = (String) authentication.getCredentials();
        try {
            Claims claims = JwtUtil.parseToken(token);
            String username = claims.getSubject();
            return Mono.just(new UsernamePasswordAuthenticationToken(username, token, List.of()));
        } catch (Exception e) {
            return Mono.error(new BadCredentialsException("Invalid JWT token"));
        }
    }
}
