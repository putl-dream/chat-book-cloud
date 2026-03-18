package com.putl.articleservice.ws;

import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import org.springframework.stereotype.Component;

@Component
public class CacheArticleHandler extends AbstractArticleHandler {

    @Override
    public String getType() {
        return "CACHE";
    }

    @Override
    protected void doHandle(String userId, ArticleMessage message) {
        articleCache.put(userId, message);
        messagePublisher.sendToUser(userId, WebSocketResult.of("CACHE", "已缓存"));
    }
}
