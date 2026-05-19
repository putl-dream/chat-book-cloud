package com.putl.agentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Agent服务应用启动类
 * <p>负责初始化并启动AI Agent微服务，提供文章生成、优化、对话等智能功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication(scanBasePackages = "com.putl")
@ServletComponentScan
@MapperScan("com.putl.agentservice.mapper")
@EnableFeignClients(basePackages = {"com.putl.agentservice.client", "com.putl.articleservice.api"})
public class AgentApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
