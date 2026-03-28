package com.putl.agentservice.service.impl;

import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.mapper.AgentMessageMapper;
import com.putl.agentservice.mapper.entity.AgentMessageDO;
import com.putl.agentservice.service.AgentConversationWindowService;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AgentConversationWindowServiceImpl implements AgentConversationWindowService {

    private final AgentMessageMapper agentMessageMapper;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final AgentChatProperties properties;

    @Value("${spring.profiles.active:local}")
    private String env;

    public AgentConversationWindowServiceImpl(AgentMessageMapper agentMessageMapper,
                                              RedisTemplate<String, Object> objectRedisTemplate,
                                              AgentChatProperties properties) {
        this.agentMessageMapper = agentMessageMapper;
        this.objectRedisTemplate = objectRedisTemplate;
        this.properties = properties;
    }

    @Override
    public List<AgentMessageDO> getRecentMessages(Integer sessionId) {
        String key = recentWindowKey(sessionId);
        List<Object> cached = listOps().range(key, 0, -1);
        if (!CollectionUtils.isEmpty(cached)) {
            refreshExpire(key);
            return cached.stream()
                    .map(this::toAgentMessage)
                    .toList();
        }

        List<AgentMessageDO> recentMessages = agentMessageMapper.selectRecentMessages(sessionId, recentWindowSize());
        overwriteCache(key, recentMessages);
        return recentMessages;
    }

    @Override
    public List<AgentMessageDO> appendMessage(Integer sessionId, List<AgentMessageDO> currentWindow, AgentMessageDO message) {
        String key = recentWindowKey(sessionId);
        Long cachedSize = listOps().size(key);
        List<AgentMessageDO> updatedWindow = mergeWindow(currentWindow, message);
        if (cachedSize != null && cachedSize > 0) {
            listOps().rightPush(key, message);
            trimToRecentWindow(key);
            refreshExpire(key);
            return updatedWindow;
        }

        overwriteCache(key, updatedWindow);
        return updatedWindow;
    }

    private AgentMessageDO toAgentMessage(Object value) {
        if (value instanceof AgentMessageDO message) {
            return message;
        }
        return JsonUtil.getObjectMapper().convertValue(value, AgentMessageDO.class);
    }

    private List<AgentMessageDO> mergeWindow(List<AgentMessageDO> currentWindow, AgentMessageDO message) {
        List<AgentMessageDO> merged = new ArrayList<>();
        if (!CollectionUtils.isEmpty(currentWindow)) {
            merged.addAll(currentWindow);
        }
        merged.add(message);
        int overflow = merged.size() - recentWindowSize();
        if (overflow > 0) {
            return new ArrayList<>(merged.subList(overflow, merged.size()));
        }
        return merged;
    }

    private void overwriteCache(String key, List<AgentMessageDO> messages) {
        objectRedisTemplate.delete(key);
        if (CollectionUtils.isEmpty(messages)) {
            return;
        }
        listOps().rightPushAll(key, new ArrayList<>(messages).toArray());
        refreshExpire(key);
    }

    private void trimToRecentWindow(String key) {
        Long size = listOps().size(key);
        if (size != null && size > recentWindowSize()) {
            listOps().trim(key, size - recentWindowSize(), -1);
        }
    }

    private void refreshExpire(String key) {
        objectRedisTemplate.expire(key, ttlMinutes(), TimeUnit.MINUTES);
    }

    private int recentWindowSize() {
        return Math.max(1, properties.getRecentWindowSize());
    }

    private int ttlMinutes() {
        return Math.max(1, properties.getRecentWindowTtlMinutes());
    }

    private String recentWindowKey(Integer sessionId) {
        return "cbc:%s:agent:conversation:window:%s".formatted(env, sessionId);
    }

    private ListOperations<String, Object> listOps() {
        return objectRedisTemplate.opsForList();
    }
}
