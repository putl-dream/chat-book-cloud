package fun.amireux.chat.book.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "authentication")
public class AuthenticationProperties {

    /**
     * 对应配置文件中的 authentication.rules
     */
    private List<AuthenticationRule> rules = new ArrayList<>();

    /**
     * 内部通信密钥 (Gateway -> Microservices)
     * 用于防止绕过网关直接攻击微服务
     */
    private String internalSecret = "chat-book-cloud-internal-secret-key-2026";

    @Data
    public static class AuthenticationRule {
        private String name;
        private String pattern;
    }
}
