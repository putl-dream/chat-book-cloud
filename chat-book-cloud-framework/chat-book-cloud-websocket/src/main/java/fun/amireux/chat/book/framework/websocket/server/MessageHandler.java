package fun.amireux.chat.book.framework.websocket.server;


import fun.amireux.chat.book.framework.websocket.domain.BaseMessage;

public interface MessageHandler<T extends BaseMessage> {

    /**
     * 返回此处理器支持的消息类型
     * 例如 "chat", "notify", "order_update"
     */
    String getType();

    /**
     * 返回消息对应的类
     */
    Class<T> getMessageClass();

    /**
     * 处理消息
     */
    void handleMessage(String userId, T message);
}
