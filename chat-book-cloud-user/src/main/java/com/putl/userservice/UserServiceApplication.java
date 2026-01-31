package com.putl.userservice;

import com.putl.userservice.service.MessageService;
import com.putl.userservice.ws.ChatChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableAsync;


@EnableAsync
@SpringBootApplication
@ServletComponentScan
@EnableFeignClients
public class UserServiceApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(UserServiceApplication.class);
        ConfigurableApplicationContext configurableApplicationContext = application.run(args);
        // 解决WebSocket不能注入的问题
        ChatChannel.setMessageService(configurableApplicationContext.getBean(MessageService.class));
    }

    @EventListener
    public void handleContextClosed(ContextClosedEvent event) {
        // 在上下文关闭时清理引用
        ChatChannel.setMessageService(null);
    }
}

