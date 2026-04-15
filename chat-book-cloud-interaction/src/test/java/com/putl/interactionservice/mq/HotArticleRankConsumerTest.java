package com.putl.interactionservice.mq;

import com.putl.articleservice.api.ArticleClient;
import com.putl.interactionservice.constant.MqConstant;
import com.putl.interactionservice.mq.event.DelayedEvictHotCacheEvent;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class HotArticleRankConsumerTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ArticleClient articleClient;

    @InjectMocks
    private HotArticleRankConsumer hotArticleRankConsumer;

    @Test
    void consumeDelayedHotCacheEvictShouldAckWhenEvictionSucceeds() {
        given(articleClient.evictHotPageCache()).willReturn(CommonResult.<Void>success());

        hotArticleRankConsumer.consumeDelayedHotCacheEvict(buildEvent(1));

        verify(articleClient).evictHotPageCache();
        verifyNoInteractions(rabbitTemplate);
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
