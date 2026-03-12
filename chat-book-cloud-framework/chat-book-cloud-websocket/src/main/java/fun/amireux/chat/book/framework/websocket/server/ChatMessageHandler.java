package fun.amireux.chat.book.framework.websocket.server;


import fun.amireux.chat.book.framework.websocket.domain.ChatMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageHandler implements MessageHandler<ChatMessage> {

    @Autowired
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public Class<ChatMessage> getMessageClass() {
        return ChatMessage.class;
    }

    @Override
    public void handleMessage(String userId, ChatMessage message) {
        log.info("用户：{} 发送了消息：{}", userId, message.getContent());
        messagePublisher.sendToUser(userId, message);
    }

}
