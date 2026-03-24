package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.websocket.domain.PingMessage;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

@Component
public class PingMessageHandler implements MessageHandler<PingMessage> {

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "PING";
    }

    @Override
    public Class<PingMessage> getMessageClass() {
        return PingMessage.class;
    }

    @Override
    public void handleMessage(String userId, PingMessage message) {
        // Keep-alive reply; client may ignore raw "PONG".
        messagePublisher.sendToUser(userId, "PONG");
    }
}
