package fun.amireux.chat.book.framework.rabbitmq.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ 配置类
 */
@Slf4j
@Configuration
public class RabbitMqConfig {

    /**
     * 使用 Jackson 序列化消息
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @PostConstruct
    public void init() {
        log.info("===========================================================================");
        log.info("[RabbitMqConfig] RabbitMQ module initialized. Using Jackson2JsonMessageConverter.");
        log.info("===========================================================================");
    }
}
