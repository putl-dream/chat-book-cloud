package fun.amireux.chat.book.user.controller;

import fun.amireux.chat.book.framework.websocket.pojo.ChatMessage;
import fun.amireux.chat.book.framework.websocket.pojo.MsgType;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    // 前端发送消息到 /app/chat
    @MessageMapping("/chat")
    @SendTo("/queue/messages") // 广播到订阅了 /topic/messages 的客户端
    public ChatMessage send(ChatMessage message) {
        System.out.println("message = " + message);
        return new ChatMessage(MsgType.CHAT, message.getTo(), message.getFrom(), message.getContent());
    }
}
