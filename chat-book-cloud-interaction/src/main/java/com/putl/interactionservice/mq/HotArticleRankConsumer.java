package com.putl.interactionservice.mq;

import com.putl.articleservice.api.ArticleClient;
import com.putl.interactionservice.constant.MqConstant;
import com.putl.interactionservice.mq.event.DelayedEvictHotCacheEvent;
import com.putl.interactionservice.mq.event.HotScoreChangedEvent;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotArticleRankConsumer {

    private static final Duration HOT_EVENT_IDEMPOTENT_TTL = Duration.ofDays(1);
    private static final Duration HOT_DAY_RANK_TTL = Duration.ofDays(2);
    private static final Duration HOT_EVICT_LOCK_TTL = Duration.ofSeconds(5);
    private static final int MAX_HOT_CACHE_EVICT_ATTEMPTS = 3;
    private static final DateTimeFormatter DAY_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;
    private static final DefaultRedisScript<List> APPLY_HOT_SCORE_SCRIPT = buildApplyHotScoreScript();

    private final RedisTemplate<String, Object> redisTemplate;
    private final RabbitTemplate rabbitTemplate;
    private final ArticleClient articleClient;

    @Value("${spring.profiles.active:local}")
    private String env;

    @RabbitListener(queues = MqConstant.HOT_SCORE_CHANGED_QUEUE)
    public void consumeHotScoreChanged(HotScoreChangedEvent event) {
        if (event == null || event.getArticleId() == null || event.getDelta() == null || event.getEventId() == null) {
            log.warn("Ignore invalid hot score changed event: {}", event);
            return;
        }
        if (Double.compare(event.getDelta(), 0D) == 0) {
            return;
        }

        String articleId = String.valueOf(event.getArticleId());
        String eventKey = RedisKeyConstants.interactionHotEventIdempotent(env, event.getEventId());
        String allKey = RedisKeyConstants.interactionHotAll(env);
        String dayKey = RedisKeyConstants.interactionHotDay(env, LocalDate.now().format(DAY_FORMATTER));
        String evictLockKey = RedisKeyConstants.interactionHotEvictLock(env);

        List<?> scriptResult = redisTemplate.execute(
            APPLY_HOT_SCORE_SCRIPT,
            List.of(eventKey, allKey, dayKey, evictLockKey),
            event.getEventId(),
            String.valueOf(HOT_EVENT_IDEMPOTENT_TTL.getSeconds()),
            String.valueOf(event.getDelta()),
            articleId,
            String.valueOf(HOT_DAY_RANK_TTL.getSeconds()),
            event.getEventId(),
            String.valueOf(HOT_EVICT_LOCK_TTL.getSeconds())
        );

        boolean processed = scriptFlag(scriptResult, 0);
        boolean lockAcquired = scriptFlag(scriptResult, 1);

        if (!processed) {
            log.debug("Skip duplicated hot score changed event: {}", event.getEventId());
        }

        if (!lockAcquired) {
            return;
        }

        DelayedEvictHotCacheEvent delayedEvent = DelayedEvictHotCacheEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .sourceEventId(event.getEventId())
            .articleId(event.getArticleId())
            .attempt(1)
            .build();

        try {
            rabbitTemplate.convertAndSend(
                MqConstant.HOT_EXCHANGE,
                MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY,
                delayedEvent
            );
        } catch (Exception e) {
            redisTemplate.delete(evictLockKey);
            throw new IllegalStateException("Failed to publish delayed hot cache evict event", e);
        }
    }

    @RabbitListener(queues = MqConstant.HOT_CACHE_EVICT_EXECUTE_QUEUE)
    public void consumeDelayedHotCacheEvict(DelayedEvictHotCacheEvent event) {
        if (event == null) {
            log.warn("Ignore null delayed hot cache evict event");
            return;
        }

        int attempt = resolveAttempt(event);
        try {
            CommonResult<Void> result = articleClient.evictHotPageCache();
            if (result == null || !result.isSuccess()) {
                scheduleHotCacheEvictRetry(event, attempt, "response=" + result, null);
                return;
            }
            log.debug("Evicted hot article cache, sourceEventId: {}, articleId: {}, attempt: {}",
                event.getSourceEventId(),
                event.getArticleId(),
                attempt);
        } catch (Exception e) {
            scheduleHotCacheEvictRetry(event, attempt, "exception", e);
        }
    }

    private static boolean scriptFlag(List<?> values, int index) {
        if (values == null || values.size() <= index || values.get(index) == null) {
            return false;
        }
        Object value = values.get(index);
        if (value instanceof Number number) {
            return number.longValue() == 1L;
        }
        return "1".equals(String.valueOf(value));
    }

    private int resolveAttempt(DelayedEvictHotCacheEvent event) {
        if (event.getAttempt() == null || event.getAttempt() < 1) {
            return 1;
        }
        return event.getAttempt();
    }

    private void scheduleHotCacheEvictRetry(DelayedEvictHotCacheEvent event,
                                            int attempt,
                                            String reason,
                                            Exception exception) {
        if (attempt >= MAX_HOT_CACHE_EVICT_ATTEMPTS) {
            log.error("Failed to evict hot page cache after {} attempts, sourceEventId: {}, articleId: {}, reason: {}",
                attempt,
                event.getSourceEventId(),
                event.getArticleId(),
                reason,
                exception);
            return;
        }

        DelayedEvictHotCacheEvent retryEvent = DelayedEvictHotCacheEvent.builder()
            .eventId(UUID.randomUUID().toString())
            .sourceEventId(event.getSourceEventId())
            .articleId(event.getArticleId())
            .attempt(attempt + 1)
            .build();

        try {
            rabbitTemplate.convertAndSend(
                MqConstant.HOT_EXCHANGE,
                MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY,
                retryEvent
            );
            log.warn("Retry delayed hot cache evict scheduled, sourceEventId: {}, articleId: {}, nextAttempt: {}, reason: {}",
                event.getSourceEventId(),
                event.getArticleId(),
                attempt + 1,
                reason,
                exception);
        } catch (Exception publishException) {
            log.error("Failed to schedule delayed hot cache evict retry, sourceEventId: {}, articleId: {}, nextAttempt: {}",
                event.getSourceEventId(),
                event.getArticleId(),
                attempt + 1,
                publishException);
        }
    }

    private static DefaultRedisScript<List> buildApplyHotScoreScript() {
        DefaultRedisScript<List> script = new DefaultRedisScript<>();
        script.setResultType(List.class);
        script.setScriptText("""
            if redis.call('EXISTS', KEYS[1]) == 1 then
                local duplicateLock = redis.call('SET', KEYS[4], ARGV[6], 'NX', 'EX', ARGV[7])
                if duplicateLock then
                    return {0, 1}
                end
                return {0, 0}
            end
            redis.call('SET', KEYS[1], ARGV[1], 'EX', ARGV[2])
            redis.call('ZINCRBY', KEYS[2], ARGV[3], ARGV[4])
            redis.call('ZINCRBY', KEYS[3], ARGV[3], ARGV[4])
            redis.call('EXPIRE', KEYS[3], ARGV[5])
            local lock = redis.call('SET', KEYS[4], ARGV[6], 'NX', 'EX', ARGV[7])
            if lock then
                return {1, 1}
            end
            return {1, 0}
            """);
        return script;
    }
}
