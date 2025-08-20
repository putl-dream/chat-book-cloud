package fun.amireux.chat.book.framework.websocket.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker  // 开启STOMP协议的WebSocket
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${socket.path}")
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
        registry.setApplicationDestinationPrefixes("/app");

        // 服务端消息的前缀
        // - /topic → 一般用于 广播（一对多）。
        // - /queue → 一般用于 点对点私聊（一对一）。
        registry.enableSimpleBroker("/queue");
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        // 配置 消息大小、发送缓冲区、发送超时：
        registry.setMessageSizeLimit(2 * 1024 * 1024); // 2MB
        registry.setSendBufferSizeLimit(512 * 1024);   // 512KB
        registry.setSendTimeLimit(20 * 1000);          // 20秒
        WebSocketMessageBrokerConfigurer.super.configureWebSocketTransport(registry);
    }
}
