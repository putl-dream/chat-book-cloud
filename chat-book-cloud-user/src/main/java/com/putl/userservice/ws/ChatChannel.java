package com.putl.userservice.ws;

import com.putl.userservice.config.SessionConfig;
import com.putl.userservice.controller.vo.MessageVO;
import com.putl.userservice.service.MessageService;
import com.putl.userservice.util.MessageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@ServerEndpoint(value = "/user/chat", configurator = SessionConfig.class)
public class ChatChannel {
    private static ConcurrentHashMap<Integer, Session> sessions = new ConcurrentHashMap<>();
    private Session session;
    private Integer userId;

    private static MessageService messageService;

    public static void setMessageService(MessageService messageService) {
        ChatChannel.messageService = messageService;
    }

    // 连接打开
    @OnOpen
    public void onOpen(Session session, EndpointConfig endpointConfig) {
        session.setMaxBinaryMessageBufferSize(1024 * 1024 * 100);
        session.setMaxTextMessageBufferSize(1024 * 1024 * 100);
        this.session = session;
        HttpSession httpSession = (HttpSession) endpointConfig.getUserProperties().get(HttpSession.class.getName());
        Integer userId = (Integer) httpSession.getAttribute("userId");

        this.userId = userId;
        sessions.put(this.userId, session);
        log.info("[websocket] 新的连接：用户={} ", userId);
    }

    // 收到消息
    @OnMessage
    public void onMessage(String message) {
        log.info("[websocket] 收到消息：id={}，message={}", this.userId, message);
        Message msg = BeanUtil.toBean(message, Message.class);
        messageHandle(msg);
    }

    // 连接关闭
    @OnClose
    public void onClose(CloseReason closeReason) {
        log.info("[websocket] 连接断开：id={}，reason={}", this.userId, closeReason);
        if (this.userId != null) {
            Session remove = sessions.remove(this.userId);
            log.info("[websocket] 连接关闭：用户={}，reason={}", this.userId, closeReason.getReasonPhrase());
        }
    }

    // 连接异常
    @OnError
    public void onError(Throwable throwable) {
        log.warn("[websocket] 连接异常（EOFException）：id={}，throwable={}", this.userId, throwable.getMessage());
    }

    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("[websocket] 发送消息时发生异常", e);
            throw new RuntimeException(e);
        }
    }

    private void messageHandle(Message message) {
        log.info("[websocket] 用户={} 操作{}", userId, message.getType());
        switch (message.getType()) {
            case "SYSTEM" -> {
                sendMessage(this.session, MessageResult.messageSystem("欢迎加入聊天室！！"));
            }
            case "USER" -> {
                // 将消息持久化
                message.getData().setSenderId(this.userId);
                messageService.save(message.getData());
                Integer receiverId = message.getData().getReceiverId();
                if (sessions.containsKey(receiverId)) {
                    Session receiverSession = sessions.get(receiverId);
                    sendMessage(receiverSession, MessageResult.messageUser(message.getData()));
                }
                sendMessage(this.session, MessageResult.messageSystem("消息发送成功！"));
            }
        }
    }

    @Data
    public static class Message {
        private String type;
        private MessageVO data;
    }
}
