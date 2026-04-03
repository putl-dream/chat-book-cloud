package com.putl.agentservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication(scanBasePackages = "com.putl")
@ServletComponentScan
@MapperScan("com.putl.agentservice.mapper")
@EnableFeignClients(basePackages = {"com.putl.agentservice.client", "com.putl.articleservice.api"})
public class AgentApplication {

    public static void main(String[] args) {
        SpringApplication.run(AgentApplication.class, args);
    }
}
