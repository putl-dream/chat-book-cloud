package com.putl.userservice.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 1. 使用字符串序列化 Key
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        // 2. 使用自定义的 Jackson 序列化 Value
        RedisSerializer<Object> jsonSerializer = buildRedisSerializer();
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    private RedisSerializer<Object> buildRedisSerializer() {
        // 创建 ObjectMapper 对象
        ObjectMapper objectMapper = new ObjectMapper();

        // 关键：解决 LocalDateTime 等 JSR310 时间类型的序列化
        objectMapper.registerModule(new JavaTimeModule());

        // 可选：设置所有属性可见，以及序列化时包含类型信息（方便反序列化为 Object）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 如果你需要将 Redis 里的 JSON 直接转回具体的对象类型（而不是 LinkedHashMap），需要启用此行
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);

        // 使用 Jackson2JsonRedisSerializer 构造
        return new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
    }
}