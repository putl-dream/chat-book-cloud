package fun.amireux.chat.book.framework.websocket.server;


import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import fun.amireux.chat.book.framework.websocket.domain.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageHandler implements MessageHandler {

    @Autowired
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "chat";
    }

    @Override
    public void handleMessage(String userId, BaseMessage message) {
        ChatMessage chatMessage = (ChatMessage) message;
        System.out.println("用户：" + userId + " 发送了消息：" + chatMessage.getContent());
        messagePublisher.sendToUser(userId, chatMessage);
    }

}
