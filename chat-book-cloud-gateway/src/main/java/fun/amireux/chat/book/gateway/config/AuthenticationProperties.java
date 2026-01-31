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

    @Data
    public static class AuthenticationRule {
        private String name;
        private String pattern;
    }
}
