package com.putl.articleservice.ws;

import com.putl.articleservice.utils.MessageResult;
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
        messagePublisher.sendToUser(userId, MessageResult.messageSave("文章保存至草稿成功！！"));
    }
}
