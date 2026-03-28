package fun.amireux.chat.book.auth.config;

import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class JwtBeanConfig {

    private String jwtSecret = "chat-book";
    private String jwtIssuer = "auth-service";
    private long jwtAccessExpiration = 15;
    private long jwtRefreshExpiration = 7;

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil() {
        Duration access = jwtAccessExpiration > 0
                ? Duration.ofMinutes(jwtAccessExpiration)
                : Duration.ofMinutes(15);
        Duration refresh = jwtRefreshExpiration > 0
                ? Duration.ofDays(jwtRefreshExpiration)
                : Duration.ofDays(7);
        return new JwtUtil(jwtSecret, jwtIssuer, access, refresh);
    }
}
