package com.putl.interactionservice.service.impl;

import com.putl.articleservice.api.ArticleClient;
import com.putl.interactionservice.service.HotArticleRankService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleRankServiceImpl implements HotArticleRankService {

    private static final double VIEW_SCORE = 1D;
    private static final double PRAISE_SCORE = 3D;
    private static final double COMMENT_SCORE = 4D;
    private static final double COLLECT_SCORE = 5D;
    private static final Duration VIEW_DEDUP_TTL = Duration.ofSeconds(5);
    private static final Duration DAY_RANK_TTL = Duration.ofDays(2);
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;

    private final RedisTemplate<String, Object> redisTemplate;
    private final ArticleClient articleClient;

    @Value("${spring.profiles.active:local}")
    private String env;

    @Override
    public boolean tryAcquireViewToken(Integer articleId, Integer userId) {
        if (articleId == null) {
            return false;
        }
        if (userId == null || userId <= 0) {
            return true;
        }
        String dedupKey = RedisKeyConstants.interactionViewDedup(env, String.valueOf(userId), String.valueOf(articleId));
        try {
            Boolean fresh = redisTemplate.opsForValue().setIfAbsent(dedupKey, 1, VIEW_DEDUP_TTL);
            return Boolean.TRUE.equals(fresh);
        } catch (Exception e) {
            log.warn("Failed to acquire hot rank view dedup token, articleId: {}, userId: {}", articleId, userId, e);
            return true;
        }
    }

    @Override
    public void recordView(Integer articleId) {
        incrementScore(articleId, VIEW_SCORE);
    }

    @Override
    public void recordPraise(Integer articleId, boolean active) {
        incrementScore(articleId, active ? PRAISE_SCORE : -PRAISE_SCORE);
    }

    @Override
    public void recordCollection(Integer articleId, boolean active) {
        incrementScore(articleId, active ? COLLECT_SCORE : -COLLECT_SCORE);
    }

    @Override
    public void recordComment(Integer articleId) {
        incrementScore(articleId, COMMENT_SCORE);
    }

    private void incrementScore(Integer articleId, double delta) {
        if (articleId == null || delta == 0D) {
            return;
        }
        String member = String.valueOf(articleId);
        String allKey = RedisKeyConstants.interactionHotAll(env);
        String dayKey = RedisKeyConstants.interactionHotDay(env, LocalDate.now().format(DAY_FORMATTER));
        try {
            redisTemplate.opsForZSet().incrementScore(allKey, member, delta);
            redisTemplate.opsForZSet().incrementScore(dayKey, member, delta);
            redisTemplate.expire(dayKey, DAY_RANK_TTL);
            evictHotPageCache();
        } catch (Exception e) {
            log.warn("Failed to update hot article rank, articleId: {}, delta: {}", articleId, delta, e);
        }
    }

    private void evictHotPageCache() {
        try {
            CommonResult<Void> result = articleClient.evictHotPageCache();
            if (result == null || !result.isSuccess()) {
                log.warn("Failed to evict hot page cache after rank update, response: {}", result);
            }
        } catch (Exception e) {
            log.warn("Failed to evict hot page cache after rank update", e);
        }
    }
}
