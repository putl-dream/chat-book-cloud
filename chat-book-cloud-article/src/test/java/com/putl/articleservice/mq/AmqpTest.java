package com.putl.articleservice.mq;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
public class AmqpTest {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @Test
    public void sendMessage() {
        String queueName = "ice.queue";
        String message = "Hello, RabbitMQ!";


        rabbitTemplate.convertAndSend(queueName, message);
    }
}
