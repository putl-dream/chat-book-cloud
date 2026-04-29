package com.putl.agentservice.client.engine;

import org.springframework.stereotype.Component;

@Component
public class ArticleAiRequestFactory {

    public ArticleAiRequest.Builder newRequest(String model,
                                               Integer maxOutputTokens,
                                               double temperature,
                                               String systemPrompt) {
        return ArticleAiRequest.builder()
                .model(model)
                .maxOutputTokens(maxOutputTokens)
                .temperature(temperature)
                .systemPrompt(systemPrompt);
    }
}
