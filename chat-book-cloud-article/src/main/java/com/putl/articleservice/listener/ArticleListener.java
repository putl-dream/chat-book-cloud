package com.putl.articleservice.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ArticleListener {
    @RabbitListener(queues = "ice.queue")
    public void listenIceMessage(String message) {
        log.info("收到的消息: {}", message);
    }
}
