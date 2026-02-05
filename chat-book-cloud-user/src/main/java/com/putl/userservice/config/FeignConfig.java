package com.putl.userservice.config;

import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(JwtUtil jwtUtil) {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 添加自定义请求头
                String userId = UserContext.getUserId();
                if (userId != null) {
                    String jwt = jwtUtil.generateToken(Map.of("id", Long.valueOf(userId)));
                    template.header("token", jwt);
                    template.header("X-User-Id", userId);
                    String username = UserContext.getUsername();
                    if (username != null) {
                        template.header("X-User-Name", username);
                    }
                }
            }
        };
    }
}
