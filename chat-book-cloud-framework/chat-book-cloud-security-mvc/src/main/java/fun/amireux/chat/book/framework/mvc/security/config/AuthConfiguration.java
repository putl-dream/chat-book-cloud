package fun.amireux.chat.book.framework.mvc.security.config;


import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
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
    private String JWT_SECRET;
    private long JWT_ACCESS_EXPIRATION;
    private long JWT_REFRESH_EXPIRATION;
}
