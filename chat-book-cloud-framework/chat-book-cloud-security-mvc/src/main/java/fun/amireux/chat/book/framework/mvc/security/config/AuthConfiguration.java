package fun.amireux.chat.book.framework.mvc.security.config;


import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfiguration {
    // ===================================== 白名单 配置 ==============================================
    private List<String> auth_whiteList = new ArrayList<>();

    // ===================================== 鉴权 配置 ==============================================
    private String JWT_SECRET = "chat-book";
    private String JWT_ISSUER = "auth-service";
    private long JWT_ACCESS_EXPIRATION;
    private long JWT_REFRESH_EXPIRATION;

    @Bean
    @ConditionalOnMissingBean
    public JwtUtil jwtUtil() {
        return new JwtUtil(JWT_SECRET, JWT_ISSUER);
    }
}
