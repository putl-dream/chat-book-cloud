package com.putl.articleservice.ws;

import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import org.springframework.stereotype.Component;

@Component
public class SaveArticleHandler extends AbstractArticleHandler {

    @Override
    public String getType() {
        return "SAVE";
    }

    @Override
    protected void doHandle(String userId, ArticleMessage message) {
        if (message.getData() == null) return;
        
        if (message.getData().getId() == null) {
            articleService.addArticle(message.getData());
        } else {
            articleService.updateArticle(message.getData());
        }
        messagePublisher.sendToUser(userId, WebSocketResult.of("SAVE", "文章保存至草稿成功！！"));
    }
}
