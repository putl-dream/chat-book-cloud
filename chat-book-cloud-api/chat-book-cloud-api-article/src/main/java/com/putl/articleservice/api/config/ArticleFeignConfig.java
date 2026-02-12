package com.putl.articleservice.api.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.putl.articleservice.api")
public class ArticleFeignConfig {
}
