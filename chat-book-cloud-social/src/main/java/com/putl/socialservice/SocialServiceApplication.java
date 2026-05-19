package com.putl.socialservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 社交服务应用启动类
 * <p>负责初始化并启动社交微服务，提供好友关系、社交动态、消息通知等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.putl")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.putl.userservice.api", "com.putl.chatservice.api"})
@MapperScan("com.putl.socialservice.mapper")
public class SocialServiceApplication {
    
    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(SocialServiceApplication.class, args);
    }
}
