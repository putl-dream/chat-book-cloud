package com.putl.interactionservice.mq;

import com.putl.articleservice.api.ArticleClient;
import com.putl.interactionservice.constant.MqConstant;
import com.putl.interactionservice.mq.event.DelayedEvictHotCacheEvent;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HotArticleRankConsumerTest {

    private static final String ENV = "local";

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ArticleClient articleClient;

    @InjectMocks
    private HotArticleRankConsumer hotArticleRankConsumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hotArticleRankConsumer, "env", ENV);
    }

    @Test
    void consumeDelayedHotCacheEvictShouldAckWhenEvictionSucceeds() {
        given(articleClient.evictHotPageCache()).willReturn(CommonResult.<Void>success());

        hotArticleRankConsumer.consumeDelayedHotCacheEvict(buildEvent(1));

        verify(articleClient).evictHotPageCache();
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void consumeHotScoreChangedShouldExecuteLuaWithStringSerializer() {
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        given(redisTemplate.getStringSerializer()).willReturn(stringSerializer);
        given(redisTemplate.execute(
            any(),
            same(stringSerializer),
            isNull(),
            anyList(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any(),
            any()
        )).willReturn(java.util.List.of(1L, 1L));

        hotArticleRankConsumer.consumeHotScoreChanged(com.putl.interactionservice.mq.event.HotScoreChangedEvent.builder()
            .eventId("hot-event-1")
            .articleId(12)
            .delta(1.5D)
            .actionType("VIEW")
            .build());

        verify(redisTemplate).execute(
            any(),
            same(stringSerializer),
            isNull(),
            eq(java.util.List.of(
                RedisKeyConstants.interactionHotEventIdempotent(ENV, "hot-event-1"),
                RedisKeyConstants.interactionHotAll(ENV),
                RedisKeyConstants.interactionHotDay(ENV, LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)),
                RedisKeyConstants.interactionHotEvictLock(ENV)
            )),
            eq("hot-event-1"),
            eq("86400"),
            eq("1.5"),
            eq("12"),
            eq("172800"),
            eq("hot-event-1"),
            eq("5")
        );
        verify(rabbitTemplate).convertAndSend(
            eq(MqConstant.HOT_EXCHANGE),
            eq(MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY),
            any(DelayedEvictHotCacheEvent.class)
        );
    }

    @Test
    void consumeDelayedHotCacheEvictShouldRetryWhenEvictionReturnsFailure() {
        given(articleClient.evictHotPageCache()).willReturn(CommonResult.<Void>error(500, "Article service unavailable"));

        hotArticleRankConsumer.consumeDelayedHotCacheEvict(buildEvent(1));

        ArgumentCaptor<DelayedEvictHotCacheEvent> eventCaptor = ArgumentCaptor.forClass(DelayedEvictHotCacheEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(MqConstant.HOT_EXCHANGE),
            eq(MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY),
            eventCaptor.capture()
        );
        DelayedEvictHotCacheEvent retryEvent = eventCaptor.getValue();
        assertThat(retryEvent.getSourceEventId()).isEqualTo("source-1");
        assertThat(retryEvent.getArticleId()).isEqualTo(12);
        assertThat(retryEvent.getAttempt()).isEqualTo(2);
    }

    @Test
    void consumeDelayedHotCacheEvictShouldTreatMissingAttemptAsFirstAttempt() {
        given(articleClient.evictHotPageCache()).willReturn(CommonResult.<Void>error(500, "Article service unavailable"));

        hotArticleRankConsumer.consumeDelayedHotCacheEvict(buildEvent(null));

        ArgumentCaptor<DelayedEvictHotCacheEvent> eventCaptor = ArgumentCaptor.forClass(DelayedEvictHotCacheEvent.class);
        verify(rabbitTemplate).convertAndSend(
            eq(MqConstant.HOT_EXCHANGE),
            eq(MqConstant.HOT_CACHE_EVICT_DELAY_ROUTING_KEY),
            eventCaptor.capture()
        );
        assertThat(eventCaptor.getValue().getAttempt()).isEqualTo(2);
    }

    @Test
    void consumeDelayedHotCacheEvictShouldStopRetryAfterMaxAttempts() {
        given(articleClient.evictHotPageCache()).willReturn(CommonResult.<Void>error(500, "Article service unavailable"));

        hotArticleRankConsumer.consumeDelayedHotCacheEvict(buildEvent(3));

        verify(articleClient).evictHotPageCache();
        verifyNoInteractions(rabbitTemplate);
    }

    private DelayedEvictHotCacheEvent buildEvent(Integer attempt) {
        return DelayedEvictHotCacheEvent.builder()
            .eventId("event-1")
            .sourceEventId("source-1")
            .articleId(12)
            .attempt(attempt)
            .build();
    }
}
