package com.putl.articleservice;

import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.ws.ArticleChannel;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.ConfigurableApplicationContext;

@EnableFeignClients
@SpringBootApplication
@ServletComponentScan
public class ArticleApplication {
    public static void main(String[] args){
        SpringApplication application = new SpringApplication(ArticleApplication.class);
        ConfigurableApplicationContext configurableApplicationContext = application.run(args);
        //解决WebSocket不能注入的问题
        ArticleChannel.setArticleService(configurableApplicationContext.getBean(ArticleService.class));
    }
}
