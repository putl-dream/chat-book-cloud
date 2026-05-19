package com.putl.articleservice;

import fun.amireux.chat.book.framework.mvc.security.config.SecurityMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

/**
 * 文章服务应用启动类
 * <p>负责初始化并启动文章微服务，提供文章管理、草稿处理、内容发布等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@EnableFeignClients(basePackages = {"com.putl.userservice.api", "com.putl.interactionservice.api"})
@SpringBootApplication(scanBasePackages = "com.putl")
@ServletComponentScan
@Import(SecurityMvcConfig.class)
public class ArticleApplication {
    
    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args){
        SpringApplication.run(ArticleApplication.class, args);
    }
}
