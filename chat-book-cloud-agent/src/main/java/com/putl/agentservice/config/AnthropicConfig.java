package com.putl.agentservice.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AnthropicProperties.class)
public class AnthropicConfig {

    @Bean(destroyMethod = "close")
    public AnthropicClient anthropicClient(AnthropicProperties properties) {
        AnthropicProperties.Anthropic anthropic = properties.getAnthropic();
        AnthropicOkHttpClient.Builder builder = AnthropicOkHttpClient.builder()
                .baseUrl(anthropic.getBaseUrl())
                .timeout(Duration.ofMillis(anthropic.getTimeoutMs()));
        if (StringUtils.hasText(anthropic.getApiKey())) {
            builder.apiKey(anthropic.getApiKey());
        }
        return builder.build();
    }
}
