package com.putl.agentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agent.ai")
public class AnthropicProperties {

    private String provider = "anthropic";

    private Anthropic anthropic = new Anthropic();

    private Codex codex = new Codex();

    public boolean isCodexProvider() {
        return "codex".equalsIgnoreCase(provider) || "openai".equalsIgnoreCase(provider);
    }

    public Model activeModel() {
        return isCodexProvider() ? codex.getModel() : anthropic.getModel();
    }

    public MaxTokens activeMaxTokens() {
        return isCodexProvider() ? codex.getMaxTokens() : anthropic.getMaxTokens();
    }

    @Data
    public static class Anthropic {
        private String apiKey;
        private String baseUrl = "https://api.minimaxi.com/anthropic";
        private Model model = new Model();
        private MaxTokens maxTokens = new MaxTokens();
        private Integer timeoutMs = 300000;
    }

    @Data
    public static class Codex {
        private String apiKey;
        private String baseUrl = "https://api.openai.com";
        private Model model = new Model("gpt-5.2");
        private MaxTokens maxTokens = new MaxTokens();
        private Integer timeoutMs = 300000;
        private String reasoningEffort;
        private Boolean store = false;
    }

    @Data
    public static class Model {
        private String chat;
        private String generate;
        private String optimize;
        private String notebook;

        public Model() {
            this("claude-sonnet-4-5");
        }

        public Model(String defaultModel) {
            this.chat = defaultModel;
            this.generate = defaultModel;
            this.optimize = defaultModel;
            this.notebook = defaultModel;
        }
    }

    @Data
    public static class MaxTokens {
        private Integer chat = 30000;
        private Integer generate = 30000;
        private Integer optimize = 30000;
        private Integer notebook = 30000;
    }
}
