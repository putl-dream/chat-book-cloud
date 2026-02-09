package com.putl.articleservice.ws;

import org.springframework.stereotype.Component;

@Component
public class CacheArticleHandler extends AbstractArticleHandler {

    @Override
    public String getType() {
        return "CACHE";
    }

    @Override
    protected void doHandle(String userId, ArticleMessage message) {
        // Just caching, handled in base class
    }
}
