package com.putl.articleservice;

import fun.amireux.chat.book.framework.mvc.security.config.SecurityMvcConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Import;

@EnableFeignClients(basePackages = {"com.putl.userservice.api", "com.putl.interactionservice.api"})
@SpringBootApplication
@ServletComponentScan
@Import(SecurityMvcConfig.class)
public class ArticleApplication {
    public static void main(String[] args){
        SpringApplication.run(ArticleApplication.class, args);
    }
}
