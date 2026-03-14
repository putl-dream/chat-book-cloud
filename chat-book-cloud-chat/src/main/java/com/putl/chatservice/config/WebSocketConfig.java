package com.putl.chatservice.config;

import com.putl.chatservice.ws.ChatWebSocketHandler;
import fun.amireux.chat.book.framework.websocket.server.AuthHandshakeInterceptor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Chat Service WebSocket 配置
 */
@Slf4j
@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    @Value("${websocket.path:/chat/ws}")
    private String WS_PATH;

    private final ChatWebSocketHandler chatWebSocketHandler;
    private final AuthHandshakeInterceptor authHandshakeInterceptor;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(chatWebSocketHandler, WS_PATH)
                .addInterceptors(authHandshakeInterceptor)
                .setAllowedOrigins("*");

        log.info("===========================================================================");
        log.info("[ChatWebSocketConfig] Chat WebSocket endpoint registered: {}", WS_PATH);
        log.info("===========================================================================");
    }
}
