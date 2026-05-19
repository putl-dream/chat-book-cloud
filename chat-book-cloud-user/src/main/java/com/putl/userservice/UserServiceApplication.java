package com.putl.userservice;

import fun.amireux.chat.book.framework.mvc.security.config.SecurityMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * 用户服务应用启动类
 * <p>负责初始化并启动用户微服务，提供用户管理、个人信息、权限控制等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@EnableAsync
@SpringBootApplication(scanBasePackages = "com.putl")
@ServletComponentScan
@EnableFeignClients(basePackages = {"com.putl.userservice.api", "com.putl.articleservice.api"})
@Import(SecurityMvcConfig.class)
public class UserServiceApplication {

    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

