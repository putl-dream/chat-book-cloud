package com.putl.articleservice.ws;

import com.putl.articleservice.service.ArticleService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;

public abstract class AbstractArticleHandler implements MessageHandler<ArticleMessage> {

    @Resource
    protected ArticleSessionCache articleCache;
    
    @Resource
    protected MessagePublisher messagePublisher;
    
    @Resource
    protected ArticleService articleService;

    @Override
    public Class<ArticleMessage> getMessageClass() {
        return ArticleMessage.class;
    }

    @Override
    public void handleMessage(String userId, ArticleMessage message) {
        // Cache the message
        articleCache.put(userId, message);
        
        // Notify cache success (from original logic)
        messagePublisher.sendToUser(userId, WebSocketResult.of("CACHE", "缓存成功!"));
        
        doHandle(userId, message);
    }

    protected abstract void doHandle(String userId, ArticleMessage message);
}
