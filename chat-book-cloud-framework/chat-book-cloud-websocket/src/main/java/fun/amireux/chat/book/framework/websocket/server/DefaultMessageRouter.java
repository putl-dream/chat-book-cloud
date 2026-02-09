package fun.amireux.chat.book.framework.websocket.server;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public DefaultMessageRouter(List<MessageHandler> handlerList) {
        // 自动收集所有实现 MessageHandler 的 Bean
        handlerList.forEach(handler -> handlers.put(handler.getType(), handler));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void route(String userId, String rawMessage) {
        try {
            // 1. 解析为 JsonNode 以获取 type 字段
            JsonNode jsonNode = objectMapper.readTree(rawMessage);
            if (!jsonNode.has("type")) {
                log.warn("消息格式错误，缺少 type 字段: {}", rawMessage);
                return;
            }
            String type = jsonNode.get("type").asText();

            // 2. 查找处理器
            MessageHandler handler = handlers.get(type);
            if (handler == null) {
                log.warn("未找到该消息类型的处理程序: {}", type);
                return;
            }

            // 3. 解析为具体的 Message 类
            Class<? extends BaseMessage> messageClass = handler.getMessageClass();
            BaseMessage message = BeanUtil.toBean(rawMessage, messageClass);

            // 4. 调用处理
            handler.handleMessage(userId, message);
        } catch (Exception e) {
            log.error("处理消息时发生错误: {}", e.getMessage(), e);
        }
    }
}
