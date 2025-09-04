package fun.amireux.chat.book.framework.websocket.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker  // 开启STOMP协议的WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${socket.path:/ws}")
    private String path;

    @PostConstruct
    public void init() {
        System.out.println("path: " + path);
    }


    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 前端连接地址： ws://localhost:8080/ws
        registry.addEndpoint(path)
                .setAllowedOriginPatterns("*") // 允许跨域
                .withSockJS(); // 开启SockJS支持
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 服务端接收消息的前缀
        registry.setApplicationDestinationPrefixes("/ws");

        // 服务端消息的前缀
        // - /topic → 一般用于 广播（所有订阅者都能收到）。
        // - /queue → 一般用于 队列（消息只会被 一个 订阅者消费）。
        registry.enableSimpleBroker("/queue");
        registry.setUserDestinationPrefix("/user");        // ✅ 关键：用户前缀
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 配置 消息大小、发送缓冲区、发送超时：
        registry.setMessageSizeLimit(2 * 1024 * 1024); // 2MB
        registry.setSendBufferSizeLimit(512 * 1024);   // 512KB
        registry.setSendTimeLimit(20 * 1000);          // 20秒
        WebSocketMessageBrokerConfigurer.super.configureWebSocketTransport(registry);
    }

    // 设置用户身份，进行点对点方式传输
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // ✅ 从自定义头 'user-id' 获取用户 ID
                    String userId = accessor.getNativeHeader("user-id") != null && !accessor.getNativeHeader("user-id").isEmpty()
                            ? accessor.getNativeHeader("user-id").get(0)
                            : null;

                    System.out.println("🎯 捕获 CONNECT 帧，user-id = " + userId);
                    if (userId != null) {
                        accessor.setUser(() -> userId);
                        System.out.println("✅ 已设置 Principal: " + userId);
                    }
                }
                return message;
            }
        });
    }

}
