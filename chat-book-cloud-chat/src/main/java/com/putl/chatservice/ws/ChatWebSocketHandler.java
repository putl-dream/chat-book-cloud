package com.putl.chatservice.ws;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.putl.chatservice.service.MessageService;
import com.putl.chatservice.vo.MessageVO;
import fun.amireux.chat.book.framework.websocket.domain.ChatMessage;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import fun.amireux.chat.book.framework.websocket.server.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 聊天 WebSocket 处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final MessageService messageService;
    private final MessagePublisher messagePublisher;
    private final SessionManager sessionManager;
    private final ObjectMapper objectMapper;

    /**
     * 用户 WebSocket Session 映射 (使用 String 作为 key)
     */
    private final Map<String, WebSocketSession> userSessions = new ConcurrentHashMap<>();

    /**
     * 从 WebSocketSession 获取用户ID
     */
    private String getUserIdFromSession(WebSocketSession session) {
        Object userIdAttr = session.getAttributes().get("userId");
        if (userIdAttr != null) {
            String userId = String.valueOf(userIdAttr).trim();
            if (StringUtils.hasText(userId) && !"null".equalsIgnoreCase(userId)) {
                return userId;
            }
        }
        // Fallback: 使用 senderId
        Object senderIdAttr = session.getAttributes().get("senderId");
        if (senderIdAttr != null) {
            return String.valueOf(senderIdAttr);
        }
        return null;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            String senderIdStr = getUserIdFromSession(session);
            if (senderIdStr == null) {
                log.warn("WebSocket message without user context");
                return;
            }

            ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);
            Integer senderId = Integer.parseInt(senderIdStr);
            Integer receiverId = Integer.parseInt(chatMessage.getTo());
            String content = chatMessage.getContent();
            String msgType = chatMessage.getMsgType() != null ? chatMessage.getMsgType() : "TEXT";

            // 1. 持久化消息到数据库
            MessageVO savedMessage = messageService.sendMessage(senderId, receiverId, content, msgType);

            // 2. 构建响应消息
            ChatMessage response = new ChatMessage();
            response.setFrom(String.valueOf(savedMessage.getSenderId()));
            response.setTo(String.valueOf(savedMessage.getReceiverId()));
            response.setContent(content);
            response.setTime(LocalDateTime.now());
            response.setMsgType(msgType);

            // 3. 推送给接收者（如果在线）
            messagePublisher.sendToUser(chatMessage.getTo(), response);

            // 4. 发送确认给发送者
            messagePublisher.sendToUser(senderIdStr, response);

            log.info("Message sent from user {} to user {}: {}", senderId, receiverId, content);

        } catch (Exception e) {
            log.error("Error handling chat message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.put(userId, session);
            sessionManager.register(userId, session);
            log.info("User {} connected via WebSocket", userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            sessionManager.remove(userId);
            log.info("User {} disconnected, status: {}", userId, status);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        String userId = getUserIdFromSession(session);
        if (userId != null) {
            userSessions.remove(userId);
            sessionManager.remove(userId);
        }
        log.error("WebSocket transport error for user {}: {}", userId, exception.getMessage());
    }

    /**
     * 检查用户是否在线
     */
    public boolean isUserOnline(String userId) {
        return userSessions.containsKey(userId);
    }
}
