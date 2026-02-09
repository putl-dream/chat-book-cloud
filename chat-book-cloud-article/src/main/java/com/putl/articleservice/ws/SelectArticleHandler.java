package com.putl.articleservice.ws;

import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.utils.MessageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
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
        // MessageResult.messageSelect expects JSON string of the article
        messagePublisher.sendToUser(userId, MessageResult.messageSelect(BeanUtil.toJsonString(articleVO)));
    }
}
