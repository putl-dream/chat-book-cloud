package com.putl.interactionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * 互动服务应用启动类
 * <p>负责初始化并启动互动微服务，提供点赞、评论、收藏等用户互动功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.putl")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.putl.articleservice.api", "com.putl.userservice.api"})
public class InteractionServiceApplication {
    
    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(InteractionServiceApplication.class, args);
    }
}
