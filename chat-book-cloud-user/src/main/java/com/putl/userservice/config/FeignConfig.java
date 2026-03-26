package com.putl.userservice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(JwtUtil jwtUtil) {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String userId = UserContext.getUserId();
                if (userId == null) {
                    return;
                }

                Map<String, Object> claims = new LinkedHashMap<>();
                claims.put("id", Long.valueOf(userId));

                String username = UserContext.getUsername();
                if (username != null && !username.isBlank()) {
                    claims.put("username", username);
                }

                String roles = UserContext.getRoles();
                if (roles != null && !roles.isBlank()) {
                    claims.put("roles", roles);
                }

                String jwt = jwtUtil.generateToken(claims);
                template.header("token", jwt);
                template.header("X-User-Id", userId);

                if (username != null && !username.isBlank()) {
                    template.header("X-User-Name", username);
                }

                if (roles != null && !roles.isBlank()) {
                    template.header("X-User-Roles", roles);
                }
            }
        };
    }
}
