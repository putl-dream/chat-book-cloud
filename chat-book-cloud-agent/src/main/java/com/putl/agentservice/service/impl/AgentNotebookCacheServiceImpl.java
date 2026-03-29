package com.putl.agentservice.service.impl;

import com.putl.agentservice.config.AgentChatProperties;
import com.putl.agentservice.mapper.AgentSessionMapper;
import com.putl.agentservice.mapper.entity.AgentSessionDO;
import com.putl.agentservice.model.vo.NotebookSummary;
import com.putl.agentservice.service.AgentNotebookCacheService;
import fun.amireux.chat.book.framework.common.utils.JsonUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class AgentNotebookCacheServiceImpl implements AgentNotebookCacheService {

    private final AgentSessionMapper agentSessionMapper;
    private final RedisTemplate<String, Object> objectRedisTemplate;
    private final AgentChatProperties properties;

    @Value("${spring.profiles.active:local}")
    private String env;

    public AgentNotebookCacheServiceImpl(AgentSessionMapper agentSessionMapper,
                                         RedisTemplate<String, Object> objectRedisTemplate,
                                         AgentChatProperties properties) {
        this.agentSessionMapper = agentSessionMapper;
        this.objectRedisTemplate = objectRedisTemplate;
        this.properties = properties;
    }

    @Override
    public NotebookSummary getNotebook(Integer sessionId) {
        String key = notebookKey(sessionId);
        Object cached = objectRedisTemplate.opsForValue().get(key);
        if (cached != null) {
            refreshExpire(key);
            return toNotebookSummary(cached);
        }

        AgentSessionDO session = agentSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalStateException("会话不存在: " + sessionId);
        }
        NotebookSummary notebook = parseNotebook(session.getNotebookSummary());
        cacheNotebook(sessionId, notebook);
        return notebook;
    }

    @Override
    public void cacheNotebook(Integer sessionId, NotebookSummary notebookSummary) {
        String key = notebookKey(sessionId);
        objectRedisTemplate.opsForValue().set(key, normalizeNotebook(notebookSummary), ttlMinutes(), TimeUnit.MINUTES);
    }

    @Override
    public void saveNotebook(Integer sessionId, NotebookSummary notebookSummary) {
        NotebookSummary normalized = normalizeNotebook(notebookSummary);
        agentSessionMapper.updateById(AgentSessionDO.builder()
                .id(sessionId)
                .notebookSummary(JsonUtil.toJsonString(normalized))
                .build());
        cacheNotebook(sessionId, normalized);
    }

    private NotebookSummary toNotebookSummary(Object value) {
        if (value instanceof NotebookSummary notebookSummary) {
            return normalizeNotebook(notebookSummary);
        }
        return normalizeNotebook(JsonUtil.getObjectMapper().convertValue(value, NotebookSummary.class));
    }

    private NotebookSummary parseNotebook(String value) {
        NotebookSummary parsed = JsonUtil.parseObject(value, NotebookSummary.class);
        return normalizeNotebook(parsed);
    }

    private NotebookSummary normalizeNotebook(NotebookSummary notebookSummary) {
        return Objects.requireNonNullElseGet(notebookSummary, () -> NotebookSummary.builder().build());
    }

    private int ttlMinutes() {
        return Math.max(1, properties.getNotebookTtlMinutes());
    }

    private void refreshExpire(String key) {
        objectRedisTemplate.expire(key, ttlMinutes(), TimeUnit.MINUTES);
    }

    private String notebookKey(Integer sessionId) {
        return "cbc:%s:agent:notebook:%s".formatted(env, sessionId);
    }
}
