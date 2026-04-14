package com.putl.interactionservice.config;

import com.putl.interactionservice.constant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MqConfig {

    private static final int HOT_CACHE_EVICT_DELAY_MS = 5000;

    @Bean
    public DirectExchange hotExchange() {
        return new DirectExchange(MqConstant.HOT_EXCHANGE);
    }

    @Bean
    public Queue hotScoreChangedQueue() {
        return new Queue(MqConstant.HOT_SCORE_CHANGED_QUEUE, true);
    }

    @Bean
    public Binding hotScoreChangedBinding(DirectExchange hotExchange, Queue hotScoreChangedQueue) {
        return BindingBuilder.bind(hotScoreChangedQueue)
            .to(hotExchange)
            .with(MqConstant.HOT_SCORE_CHANGED_ROUTING_KEY);
    }

    @Bean
    public Queue hotCacheEvictDelayQueue() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-message-ttl", HOT_CACHE_EVICT_DELAY_MS);
        arguments.put("x-dead-letter-exchange", MqConstant.HOT_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", MqConstant.HOT_CACHE_EVICT_EXECUTE_ROUTING_KEY);
        return new Queue(MqConstant.HOT_CACHE_EVICT_DELAY_QUEUE, true, false, false, arguments);
    }

    @Bean
    public Binding hotCacheEvictDelayBinding(DirectExchange hotExchange, Queue hotCacheEvictDelayQueue) {
        return BindingBuilder.bind(hotCacheEvictDelayQueue)
            .to(hotExchange)
            .with(MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY);
    }

    @Bean
    public Queue hotCacheEvictExecuteQueue() {
        return new Queue(MqConstant.HOT_CACHE_EVICT_EXECUTE_QUEUE, true);
    }

    @Bean
    public Binding hotCacheEvictExecuteBinding(DirectExchange hotExchange, Queue hotCacheEvictExecuteQueue) {
        return BindingBuilder.bind(hotCacheEvictExecuteQueue)
            .to(hotExchange)
            .with(MqConstant.HOT_CACHE_EVICT_EXECUTE_ROUTING_KEY);
    }
}
