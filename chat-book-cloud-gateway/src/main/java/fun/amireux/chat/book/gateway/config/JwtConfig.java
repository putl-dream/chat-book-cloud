package fun.amireux.chat.book.gateway.config;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtUtil jwtUtil() {
        return new JwtUtil("chat-book", "auth-service");
    }
}
