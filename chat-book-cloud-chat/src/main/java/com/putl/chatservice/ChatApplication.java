package com.putl.chatservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Chat Service Application - 即时通讯服务启动类
 */
@SpringBootApplication(scanBasePackages = "com.putl.chatservice")
@EnableDiscoveryClient
@MapperScan("com.putl.chatservice.mapper")
public class ChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
