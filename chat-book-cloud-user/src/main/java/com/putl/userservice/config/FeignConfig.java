package com.putl.userservice.config;

import com.putl.userservice.common.ReqInfoContext;
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
                String jwt = JwtUtil.generateToken(Map.of("id", ReqInfoContext.getReqInfo().getUserId()));
                template.header("token", jwt);
            }
        };
    }
}
