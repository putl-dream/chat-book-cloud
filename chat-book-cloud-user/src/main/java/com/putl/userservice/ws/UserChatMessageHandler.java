package com.putl.userservice.ws;

import com.putl.userservice.service.MessageService;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserChatMessageHandler implements MessageHandler<UserChatMessage> {

    @Resource
    private MessageService messageService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "USER";
    }

    @Override
    public Class<UserChatMessage> getMessageClass() {
        return UserChatMessage.class;
    }

    @Override
    public void handleMessage(String userId, UserChatMessage message) {
        log.info("[websocket] 用户={} 操作 USER", userId);
        
        if (message.getData() != null) {
            // 将消息持久化
            try {
                message.getData().setSenderId(Integer.valueOf(userId));
                messageService.save(message.getData());
                
                Integer receiverId = message.getData().getReceiverId();
                
                // 发送给接收者
                messagePublisher.sendToUser(String.valueOf(receiverId), WebSocketResult.of("USER", message.getData()));
                
                // 发送给发送者（确认）
                messagePublisher.sendToUser(userId, WebSocketResult.system("消息发送成功！"));
            } catch (NumberFormatException e) {
                log.error("用户ID格式错误: {}", userId);
            }
        }
    }
}
