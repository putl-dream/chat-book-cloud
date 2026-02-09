package com.putl.articleservice.ws;

import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import fun.amireux.chat.book.framework.websocket.server.WebSocketConnectionListener;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

@Slf4j
@Component
public class ArticleConnectionListener implements WebSocketConnectionListener {

    @Resource
    private ArticleSessionCache articleCache;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public void onConnect(String userId, WebSocketSession session) {
        ArticleMessage cached = articleCache.get(userId);
        if (cached != null && cached.getData() != null) {
            try {
                messagePublisher.sendToUser(userId, WebSocketResult.of("SELECT", cached.getData()));
            } catch (Exception e) {
                log.error("Failed to send cached message", e);
            }
        }
    }

    @Override
    public void onClose(String userId, WebSocketSession session) {
        articleCache.remove(userId);
    }
}
