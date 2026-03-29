package com.putl.userservice;

import fun.amireux.chat.book.framework.mvc.security.config.SecurityMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@ServletComponentScan
@EnableFeignClients(basePackages = {"com.putl.userservice.api", "com.putl.articleservice.api"})
@Import(SecurityMvcConfig.class)
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

