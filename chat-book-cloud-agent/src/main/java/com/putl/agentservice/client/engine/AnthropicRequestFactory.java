package com.putl.agentservice.client.engine;

import com.anthropic.models.messages.MessageCreateParams;
import com.anthropic.models.messages.MessageParam;
import com.putl.agentservice.config.AnthropicProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AnthropicRequestFactory {

    private final AnthropicProperties properties;

    /**
     * 构造 Anthropic 请求工厂
     *
     * @param properties Anthropic 配置属性
     */
    public AnthropicRequestFactory(AnthropicProperties properties) {
        this.properties = properties;
    }

    public MessageCreateParams toParams(ArticleAiRequest request) {
        validateApiKeyConfigured();
        MessageCreateParams.Builder builder = MessageCreateParams.builder()
                .model(request.getModel())
                .maxTokens(request.getMaxOutputTokens().longValue())
                .temperature(request.getTemperature())
                .system(request.getSystemPrompt());
        request.getMessages().stream()
                .map(this::toAnthropicMessage)
                .forEach(builder::addMessage);
        return builder.build();
    }

    private MessageParam toAnthropicMessage(ArticleAiRequest.Message message) {
        return MessageParam.builder()
                .role(toAnthropicRole(message.getRole()))
                .content(message.getContent())
                .build();
    }

    private MessageParam.Role toAnthropicRole(ArticleAiRequest.Role role) {
        if (role == ArticleAiRequest.Role.ASSISTANT) {
            return MessageParam.Role.ASSISTANT;
        }
        return MessageParam.Role.USER;
    }

    /**
     * 校验 API Key 是否已配置
     *
     * @throws IllegalStateException 未配置 API Key 时抛出
     */
    private void validateApiKeyConfigured() {
        if (!StringUtils.hasText(properties.getAnthropic().getApiKey())) {
            throw new IllegalStateException("ANTHROPIC_API_KEY 未配置，无法调用 Anthropic SDK");
        }
    }
}
