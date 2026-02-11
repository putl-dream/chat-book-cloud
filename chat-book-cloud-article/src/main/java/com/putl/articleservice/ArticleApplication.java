package com.putl.articleservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients(basePackages = {"com.putl.articleservice", "com.putl.userservice.api"})
@SpringBootApplication
@ServletComponentScan
public class ArticleApplication {
    public static void main(String[] args){
        SpringApplication.run(ArticleApplication.class, args);
    }
}
