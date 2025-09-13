package fun.amireux.chat.book.langchain.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ai.qwen")
public class QwenAiProperties {
    private String apiKey;
    private String chatModelName = "qwen-plus-2025-07-28";
    private String embeddingModelName = "text-embedding-v4";
    private String imageModelName = "qwen-image";
    private double temperature = 0.7;
    private String baseUrl;
}