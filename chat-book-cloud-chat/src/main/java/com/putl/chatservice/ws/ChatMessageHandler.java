package com.putl.chatservice.ws;

import com.putl.chatservice.service.MessageService;
import fun.amireux.chat.book.framework.websocket.domain.ChatMessage;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMessageHandler implements MessageHandler<ChatMessage> {

    @Resource
    private MessageService messageService;

    @Resource
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
        log.info("[websocket] 用户={} 发送聊天消息", userId);

        try {
            Integer senderId = Integer.parseInt(userId);
            Integer receiverId = Integer.parseInt(message.getTo());
            String content = message.getContent();
            String msgType = message.getMsgType() != null ? message.getMsgType() : "TEXT";

            // 1. 持久化消息到数据库
            messageService.sendMessage(senderId, receiverId, content, msgType);

            // 2. 构建响应消息
            ChatMessage response = new ChatMessage();
            response.setFrom(userId);
            response.setTo(message.getTo());
            response.setContent(content);
            response.setMsgType(msgType);

            // 3. 推送给接收者（如果在线）
            messagePublisher.sendToUser(message.getTo(), WebSocketResult.of("chat", response));

            // 4. 发送确认给发送者
            messagePublisher.sendToUser(userId, WebSocketResult.system("消息发送成功！"));

            log.info("Message sent from user {} to user {}: {}", senderId, receiverId, content);

        } catch (NumberFormatException e) {
            log.error("用户ID格式错误: {}", userId);
            messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：用户ID格式错误"));
        } catch (Exception e) {
            log.error("处理聊天消息失败: {}", e.getMessage(), e);
            messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：" + e.getMessage()));
        }
    }
}
