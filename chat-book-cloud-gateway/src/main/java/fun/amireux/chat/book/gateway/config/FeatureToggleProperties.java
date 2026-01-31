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
@ConfigurationProperties(prefix = "feature-toggle")
public class FeatureToggleProperties {

    /**
     * 对应配置文件中的 feature-toggle.rules
     */
    private List<FeatureToggleRule> rules = new ArrayList<>();

    @Data
    public static class FeatureToggleRule {
        private String name;
        private String pattern;
        private List<String> methods;
        private boolean enabled;
        private String message;
    }
}
