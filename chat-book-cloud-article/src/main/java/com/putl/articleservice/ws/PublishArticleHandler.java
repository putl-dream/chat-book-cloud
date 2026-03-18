package com.putl.articleservice.ws;

import com.putl.articleservice.controller.vo.ArticleCommandResult;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import org.springframework.stereotype.Component;

@Component
public class PublishArticleHandler extends AbstractArticleHandler {

    @Override
    public String getType() {
        return "PUBLISH";
    }

    @Override
    protected void doHandle(String userId, ArticleMessage message) {
        if (message.getData() == null) return;

        ArticleCommandResult result = articleService.publish(message.getData());
        message.getData().setId(result.getArticleId());
        message.getData().setStatus(result.getStatus());
        message.getData().setUpdatedAt(result.getUpdatedAt());
        articleCache.put(userId, message);
        messagePublisher.sendToUser(userId, WebSocketResult.of("PUBLISH", result));
    }
}
