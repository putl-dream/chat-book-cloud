package com.putl.articleservice.config;

import com.putl.articleservice.common.ReqInfoContext;
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
                ReqInfoContext.ReqInfo reqInfo = ReqInfoContext.getReqInfo();
                if (reqInfo != null) {
                    // 传递用户ID
                    template.header("X-User-Id", String.valueOf(reqInfo.getUserId()));
                    // 传递客户端IP
                    if (reqInfo.getClientIp() != null) {
                        template.header("X-Client-IP", reqInfo.getClientIp());
                    }
                    // 传递设备信息
                    if (reqInfo.getDevice() != null) {
                        template.header("X-Device", reqInfo.getDevice());
                    }
                    log.debug("[Feign] 传递用户上下文: userId={}, path={}",
                            reqInfo.getUserId(), template.path());
                } else {
                    log.warn("[Feign] ReqInfoContext 为空，无法传递用户上下文，path={}", template.path());
                }
            }
        };
    }
}
