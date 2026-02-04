package com.putl.userservice.config;

import fun.amireux.chat.book.framework.common.context.UserContext;
import com.putl.userservice.util.JwtUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                // 添加自定义请求头
                String userId = UserContext.getUserId();
                if (userId != null) {
                    String jwt = JwtUtil.generateToken(Map.of("id", Integer.parseInt(userId)));
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
