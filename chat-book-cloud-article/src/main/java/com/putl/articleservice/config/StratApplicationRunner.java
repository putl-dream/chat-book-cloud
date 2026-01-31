package com.putl.articleservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class StratApplicationRunner implements ApplicationRunner {
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(ApplicationArguments args) {
        // 清空redis缓存
        Set<String> keys = redisTemplate.keys("*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("redis data has been emptied");
        }
    }
}