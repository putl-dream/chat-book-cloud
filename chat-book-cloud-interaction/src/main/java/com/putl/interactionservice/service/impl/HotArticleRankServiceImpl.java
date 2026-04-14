package com.putl.interactionservice.service.impl;

import com.putl.interactionservice.constant.MqConstant;
import com.putl.interactionservice.mq.event.HotScoreChangedEvent;
import com.putl.interactionservice.service.HotArticleRankService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HotArticleRankServiceImpl implements HotArticleRankService {

    private static final double VIEW_SCORE = 1D;
    private static final double PRAISE_SCORE = 3D;
    private static final double COMMENT_SCORE = 4D;
    private static final double COLLECT_SCORE = 5D;
    private static final Duration VIEW_DEDUP_TTL = Duration.ofSeconds(5);

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;

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
        publishScoreChange(articleId, VIEW_SCORE, "VIEW");
    }

    @Override
    public void recordPraise(Integer articleId, boolean active) {
        publishScoreChange(articleId, active ? PRAISE_SCORE : -PRAISE_SCORE, active ? "PRAISE_ON" : "PRAISE_OFF");
    }

    @Override
    public void recordCollection(Integer articleId, boolean active) {
        publishScoreChange(articleId, active ? COLLECT_SCORE : -COLLECT_SCORE, active ? "COLLECT_ON" : "COLLECT_OFF");
    }

    @Override
    public void recordComment(Integer articleId) {
        publishScoreChange(articleId, COMMENT_SCORE, "COMMENT");
    }

    private void publishScoreChange(Integer articleId, double delta, String actionType) {
        if (articleId == null || delta == 0D) {
            return;
        }
        HotScoreChangedEvent event = HotScoreChangedEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .articleId(articleId)
            .delta(delta)
            .actionType(actionType)
            .build();

        Runnable publishTask = () -> {
            try {
                rabbitTemplate.convertAndSend(
                    MqConstant.HOT_EXCHANGE,
                    MqConstant.HOT_SCORE_CHANGED_ROUTING_KEY,
                    event
                );
            } catch (Exception e) {
                log.warn("Failed to publish hot score changed event, articleId: {}, delta: {}, actionType: {}",
                    articleId, delta, actionType, e);
            }
        };

        if (TransactionSynchronizationManager.isActualTransactionActive()
            && TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    publishTask.run();
                }
            });
            return;
        }
        publishTask.run();
    }
}
