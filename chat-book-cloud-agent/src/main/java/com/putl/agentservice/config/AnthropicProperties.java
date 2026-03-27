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
        private String baseUrl = "https://api.anthropic.com";
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
        private Integer chat = 2048;
        private Integer generate = 4096;
        private Integer optimize = 4096;
        private Integer notebook = 2048;
    }
}
