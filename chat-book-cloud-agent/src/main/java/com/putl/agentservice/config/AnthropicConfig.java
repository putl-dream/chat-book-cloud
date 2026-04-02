package com.putl.agentservice.config;

import com.anthropic.client.AnthropicClient;
import com.anthropic.client.okhttp.AnthropicOkHttpClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Slf4j
@Configuration
@EnableConfigurationProperties({AnthropicProperties.class, AgentChatProperties.class})
public class AnthropicConfig {

    private static final long MIN_TIMEOUT_MS = 300000L;

    @Bean(destroyMethod = "close")
    public AnthropicClient anthropicClient(AnthropicProperties properties) {
        AnthropicProperties.Anthropic anthropic = properties.getAnthropic();
        long configuredTimeoutMs = anthropic.getTimeoutMs() == null ? MIN_TIMEOUT_MS : anthropic.getTimeoutMs();
        long effectiveTimeoutMs = Math.max(MIN_TIMEOUT_MS, configuredTimeoutMs);
        if (configuredTimeoutMs < MIN_TIMEOUT_MS) {
            log.warn("Configured Anthropic timeout {}ms is too low for long-running draft streams; using {}ms instead",
                    configuredTimeoutMs, effectiveTimeoutMs);
        }
        AnthropicOkHttpClient.Builder builder = AnthropicOkHttpClient.builder()
                .baseUrl(anthropic.getBaseUrl())
                .timeout(Duration.ofMillis(effectiveTimeoutMs));
        if (StringUtils.hasText(anthropic.getApiKey())) {
            builder.apiKey(anthropic.getApiKey());
        }
        return builder.build();
    }
}
