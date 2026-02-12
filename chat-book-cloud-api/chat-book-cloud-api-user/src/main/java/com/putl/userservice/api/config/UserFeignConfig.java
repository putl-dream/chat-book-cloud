package com.putl.userservice.api.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "com.putl.userservice.api")
public class UserFeignConfig {
}
