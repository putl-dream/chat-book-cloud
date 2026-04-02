package com.putl.agentservice.client.engine;

import com.anthropic.models.messages.MessageCreateParams;

public interface ArticleAiTask<T> {

    String taskCode();

    MessageCreateParams createParams(ArticleAiContext context);

    T parseResponse(String rawText);

    default boolean supportsStreaming() {
        return false;
    }
}
