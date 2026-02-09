package com.putl.articleservice.ws;

import com.putl.articleservice.controller.vo.ArticleVO;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import org.springframework.stereotype.Component;

@Component
public class SelectArticleHandler extends AbstractArticleHandler {

    @Override
    public String getType() {
        return "SELECT";
    }

    @Override
    protected void doHandle(String userId, ArticleMessage message) {
        if (message.getData() == null || message.getData().getId() == null) return;

        ArticleVO articleVO = articleService.getArticleDetail(message.getData().getId());
        messagePublisher.sendToUser(userId, WebSocketResult.of("SELECT", articleVO));
    }
}
