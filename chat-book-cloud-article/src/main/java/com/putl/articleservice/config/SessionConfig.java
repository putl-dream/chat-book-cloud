package com.putl.articleservice.config;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;


public class SessionConfig extends ServerEndpointConfig.Configurator {
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response){
        // 优先从 Header 中获取用户 ID (由网关传递)
        var headers = request.getHeaders();
        var userIds = headers.get("X-User-Id");
        if (userIds != null && !userIds.isEmpty()) {
            sec.getUserProperties().put("userId", userIds.get(0));
        }

        // 兼容旧的 HttpSession 方式
        HttpSession httpSession = (HttpSession) request.getHttpSession();
        if (httpSession != null) {
            sec.getUserProperties().put(HttpSession.class.getName(), httpSession);
            if (httpSession.getAttribute("userId") != null) {
                sec.getUserProperties().put("userId", String.valueOf(httpSession.getAttribute("userId")));
            }
        }
    }
}