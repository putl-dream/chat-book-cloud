package com.putl.agentservice.client.engine;

import com.anthropic.models.messages.MessageCreateParams;
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

    /**
     * 创建新的请求参数构建器
     *
     * @param model        模型名称
     * @param maxTokens    最大 token 数
     * @param temperature  温度参数
     * @param systemPrompt 系统提示词
     * @return MessageCreateParams 构建器
     */
    public MessageCreateParams.Builder newRequest(String model,
                                                  Integer maxTokens,
                                                  double temperature,
                                                  String systemPrompt) {
        validateApiKeyConfigured();
        return MessageCreateParams.builder()
                .model(model)
                .maxTokens(maxTokens.longValue())
                .temperature(temperature)
                .system(systemPrompt);
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
