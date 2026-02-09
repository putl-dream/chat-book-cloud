package com.putl.articleservice.ws;

import com.putl.articleservice.controller.vo.ArticleVO;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ArticleMessage extends BaseMessage {
    private ArticleVO data;
}
