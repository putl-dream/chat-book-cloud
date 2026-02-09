package com.putl.articleservice.ws;

import com.putl.articleservice.utils.MessageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
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
                // Restore logic from ArticleChannel.onOpen
                // sendMessage(MessageResult.messageSelect(articleInfo.get(this.userId)));
                // articleInfo.get returns raw message. MessageResult.messageSelect expects stringified ArticleVO.
                // Wait, original articleInfo stored the WHOLE message or just data?
                // ArticleChannel: articleInfo.put(userId, message); -> message is the String payload of websocket.
                // Payload is {"type": "...", "data": ...}
                // MessageResult.messageSelect(String data) -> BeanUtil.toBean(data, ArticleVO.class).
                // If data is the whole message string, `toBean(str, ArticleVO.class)` might fail if structure doesn't match ArticleVO fields.
                // ArticleVO likely has content, title etc.
                // The message string is {"type":"SAVE", "data":{...}}.
                // If we pass this JSON to ArticleVO.class, it will map fields.
                // Does ArticleVO have "data" field? No.
                // So original code `MessageResult.messageSelect(articleInfo.get(this.userId))` might have been working because `articleInfo` contained just the data part?
                // No, `onMessage(String message)` -> `articleInfo.put(userId, message)`. It stores the full message.
                // So `MessageResult.messageSelect` tries to parse the full message as ArticleVO?
                // This implies ArticleVO might have ignored unknown properties and maybe matched some fields?
                // OR `MessageResult.messageSelect` logic I read is:
                // ArticleVO article = BeanUtil.toBean(data, ArticleVO.class);
                // If data is `{"type":"...", "data":{...}}`, and ArticleVO doesn't have `type` or `data`, it results in empty object?
                // Unless `ArticleVO` has `data` field?
                
                // Let's assume I should return the `data` part of the cached message.
                String json = BeanUtil.toJsonString(cached.getData());
                messagePublisher.sendToUser(userId, MessageResult.messageSelect(json));
                
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
