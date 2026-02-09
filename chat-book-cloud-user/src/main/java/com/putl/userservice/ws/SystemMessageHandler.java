package com.putl.userservice.ws;

import com.putl.userservice.util.MessageResult;
import fun.amireux.chat.book.framework.websocket.server.MessageHandler;
import fun.amireux.chat.book.framework.websocket.server.MessagePublisher;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SystemMessageHandler implements MessageHandler<SystemMessage> {

    @Resource
    private MessagePublisher messagePublisher;

    @Override
    public String getType() {
        return "SYSTEM";
    }

    @Override
    public Class<SystemMessage> getMessageClass() {
        return SystemMessage.class;
    }

    @Override
    public void handleMessage(String userId, SystemMessage message) {
        log.info("[websocket] 用户={} 操作 SYSTEM", userId);
        messagePublisher.sendToUser(userId, MessageResult.messageSystem("欢迎加入聊天室！！"));
    }
}
