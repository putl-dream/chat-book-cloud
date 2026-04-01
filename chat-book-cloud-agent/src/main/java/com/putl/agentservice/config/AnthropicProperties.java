package com.putl.agentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agent.ai")
public class AnthropicProperties {

    private String provider = "anthropic";

    private Anthropic anthropic = new Anthropic();

    @Data
    public static class Anthropic {
        private String apiKey;
        private String baseUrl = "https://https://api.minimaxi.com/anthropic";
        private Model model = new Model();
        private MaxTokens maxTokens = new MaxTokens();
        private Integer timeoutMs = 30000;
    }

    @Data
    public static class Model {
        private String chat = "claude-sonnet-4-5";
        private String generate = "claude-sonnet-4-5";
        private String optimize = "claude-sonnet-4-5";
        private String notebook = "claude-sonnet-4-5";
    }

    @Data
    public static class MaxTokens {
        private Integer chat = 30000;
        private Integer generate = 30000;
        private Integer optimize = 30000;
        private Integer notebook = 30000;
    }
}
