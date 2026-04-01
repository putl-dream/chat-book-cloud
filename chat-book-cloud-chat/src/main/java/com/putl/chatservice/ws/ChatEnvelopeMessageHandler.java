package com.putl.chatservice.ws;

import com.putl.chatservice.service.MessageService;
import fun.amireux.chat.book.framework.websocket.domain.ChatMessage;
import fun.amireux.chat.book.framework.websocket.domain.WebSocketResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
public class ChatEnvelopeMessageHandler implements MessageHandler<ChatEnvelopeMessage> {

    @Resource
    private MessageService messageService;

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "CHAT";
    }

    @Override
    public Class<ChatEnvelopeMessage> getMessageClass() {
        return ChatEnvelopeMessage.class;
    }

    @Override
    public void handleMessage(String userId, ChatEnvelopeMessage message) {
        log.info("[websocket] 用户={} 发送聊天消息", userId);

        try {
            ChatEnvelopeData data = message.getData();
            if (data == null || !StringUtils.hasText(data.getContent())) {
                messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：消息内容不能为空"));
                return;
            }

            Integer senderId = Integer.parseInt(userId);
            Integer receiverId = resolveReceiverId(data);
            String content = data.getContent().trim();
            String msgType = StringUtils.hasText(data.getMsgType()) ? data.getMsgType() : "TEXT";

            messageService.sendMessage(senderId, receiverId, content, msgType);

            ChatMessage response = new ChatMessage();
            response.setFrom(userId);
            response.setTo(String.valueOf(receiverId));
            response.setContent(content);
            response.setMsgType(msgType);

            messagePublisher.sendToUser(String.valueOf(receiverId), WebSocketResult.of("CHAT", response));
            messagePublisher.sendToUser(userId, WebSocketResult.system("消息发送成功！"));

        } catch (NumberFormatException e) {
            log.error("用户ID格式错误: {}", userId, e);
            messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：用户ID格式错误"));
        } catch (IllegalArgumentException e) {
            messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：" + e.getMessage()));
        } catch (Exception e) {
            log.error("处理聊天消息失败: {}", e.getMessage(), e);
            messagePublisher.sendToUser(userId, WebSocketResult.error("消息发送失败：" + e.getMessage()));
        }
    }

    private Integer resolveReceiverId(ChatEnvelopeData data) {
        if (data.getReceiverId() != null) {
            return data.getReceiverId();
        }
        if (StringUtils.hasText(data.getTo())) {
            return Integer.parseInt(data.getTo());
        }
        throw new IllegalArgumentException("接收方不能为空");
    }
}
