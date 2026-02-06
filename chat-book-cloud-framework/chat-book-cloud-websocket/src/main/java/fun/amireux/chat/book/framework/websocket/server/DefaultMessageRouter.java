package fun.amireux.chat.book.framework.websocket.server;

import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class DefaultMessageRouter implements MessageRouter {

    private final Map<String, MessageHandler> handlers = new HashMap<>();

    @Autowired
    public DefaultMessageRouter(List<MessageHandler> handlerList) {
        // 自动收集所有实现 MessageHandler 的 Bean
        handlerList.forEach(handler -> handlers.put(handler.getType(), handler));
    }

    @Override
    public void route(String userId, String rawMessage) {
        try {
            // 解析消息结构

            var base = BeanUtil.toBean(rawMessage, BaseMessage.class);
            String type = base.getType();

            MessageHandler handler = handlers.get(type);
            if (handler == null) {
                log.warn("未找到该消息类型的处理程序: {}", type);
                return;
            }

            handler.handleMessage(userId, base);
        } catch (Exception e) {
            log.error("处理消息时发生错误: {}", e.getMessage(), e);
        }
    }
}
