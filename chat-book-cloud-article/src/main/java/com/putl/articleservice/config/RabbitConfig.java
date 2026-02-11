package com.putl.articleservice.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue iceQueue() {
        return new Queue("ice.queue", true); // durable = true by default
    }
}
