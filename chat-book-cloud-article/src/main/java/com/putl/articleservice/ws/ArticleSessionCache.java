package com.putl.articleservice.ws;

import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ArticleSessionCache {
    private final Map<String, ArticleMessage> cache = new ConcurrentHashMap<>();

    public void put(String userId, ArticleMessage message) {
        cache.put(userId, message);
    }

    public ArticleMessage get(String userId) {
        return cache.get(userId);
    }

    public void remove(String userId) {
        cache.remove(userId);
    }
}
