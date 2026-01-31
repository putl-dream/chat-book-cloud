package com.putl.articleservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching // 启用缓存
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory redisConnectionFactory) {
        // 定义Jackson2JsonRedisSerializer序列化对象
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // 注册Java 8日期模块

        // 创建针对Redis的JSON序列化器，传入自定义的ObjectMapper
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 默认的缓存配置
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30)) // 设置默认的过期时间为30分钟
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(serializer)); // 使用JSON序列化

        // 特定缓存区域(articleCache)的配置
        RedisCacheConfiguration articleCacheConfig = defaultCacheConfig.entryTtl(Duration.ofHours(1)); // 设置articleCache的过期时间为1小时

        return RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withCacheConfiguration("articleCache", articleCacheConfig) // 为特定缓存区域设置配置
                .build();
    }

}