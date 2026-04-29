package com.putl.agentservice.client.engine;

public interface ArticleAiTask<T> {

    String taskCode();

    ArticleAiRequest createRequest(ArticleAiContext context);

    T parseResponse(String rawText);

    default boolean supportsStreaming() {
        return false;
    }
}
