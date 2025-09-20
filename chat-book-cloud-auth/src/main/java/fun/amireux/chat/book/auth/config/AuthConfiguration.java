package fun.amireux.chat.book.auth.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfiguration {
    // ===================================== JWT 配置 ==============================================
    private String JWT_SECRET;
    private long JWT_ACCESS_EXPIRATION = 1000 * 60 * 60 * 24; // 1天
    private long JWT_REFRESH_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // 7天



}
