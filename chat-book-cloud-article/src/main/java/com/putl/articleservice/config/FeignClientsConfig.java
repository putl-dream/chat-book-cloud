package com.putl.articleservice.config;

import fun.amireux.chat.book.framework.common.context.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign 客户端配置
 * 用于在服务间调用时传递用户上下文信息
 *
 * @since 2026-01-31
 */
@Slf4j
@Configuration
public class FeignClientsConfig {

    /**
     * Feign 请求拦截器
     * 将当前请求的用户上下文信息传递到下游服务
     */
    @Bean
    public RequestInterceptor requestInterceptor() {
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate template) {
                String userId = UserContext.getUserId();
                if (userId != null) {
                    // 传递用户ID
                    template.header("X-User-Id", userId);
                    // 传递用户名
                    String username = UserContext.getUsername();
                    if (username != null) {
                        template.header("X-User-Name", username);
                    }
                    // 传递客户端IP
                    String clientIp = UserContext.getClientIp();
                    if (clientIp != null) {
                        template.header("X-Client-IP", clientIp);
                    }
                    log.debug("[Feign] 传递用户上下文: userId={}, path={}", userId, template.path());
                }
            }
        };
    }
}
