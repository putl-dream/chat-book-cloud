package com.putl.chatservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 聊天服务应用启动类
 * <p>负责初始化并启动即时通讯微服务，提供实时聊天、消息推送等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.putl.chatservice")
@EnableDiscoveryClient
@MapperScan("com.putl.chatservice.mapper")
public class ChatApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(ChatApplication.class, args);
    }
}
