package com.putl.interactionservice.service.impl;

import com.putl.interactionservice.entity.ArticleStatDO;
import com.putl.interactionservice.mapper.ArticleStatMapper;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotArticleRankServiceImplTest {

    private static final String ENV = "dev";
    private static final String HOT_ALL_KEY = RedisKeyConstants.interactionHotAll(ENV);
    private static final String HOT_INIT_LOCK_KEY = RedisKeyConstants.interactionHotInitLock(ENV);

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private ArticleStatMapper articleStatMapper;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private HotArticleRankServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new HotArticleRankServiceImpl(redisTemplate, rabbitTemplate, articleStatMapper);
        ReflectionTestUtils.setField(service, "env", ENV);
        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void initializeAllRankIfAbsentShouldReturnExistingSizeWithoutQueryingDb() {
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(3L);

        long result = service.initializeAllRankIfAbsent();

        assertThat(result).isEqualTo(3L);
        verify(articleStatMapper, never()).selectAllForHotRank();
    }

    @Test
    void initializeAllRankIfAbsentShouldWarmRedisFromArticleStats() {
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(0L, 0L, 2L);
        when(valueOperations.setIfAbsent(eq(HOT_INIT_LOCK_KEY), any(), any(Duration.class))).thenReturn(true);
        when(articleStatMapper.selectAllForHotRank()).thenReturn(List.of(
            ArticleStatDO.builder().articleId(13).viewCount(2L).praiseCount(1L).commentCount(0L).collectCount(0L).build(),
            ArticleStatDO.builder().articleId(17).viewCount(1L).praiseCount(0L).commentCount(1L).collectCount(0L).build(),
            ArticleStatDO.builder().articleId(25).viewCount(0L).praiseCount(0L).commentCount(0L).collectCount(0L).build()
        ));
        when(zSetOperations.add(HOT_ALL_KEY, 13, 5D)).thenReturn(true);
        when(zSetOperations.add(HOT_ALL_KEY, 17, 5D)).thenReturn(true);
        when(valueOperations.get(HOT_INIT_LOCK_KEY)).thenReturn("lock-value");

        long result = service.initializeAllRankIfAbsent();

        assertThat(result).isEqualTo(2L);
        verify(zSetOperations).add(HOT_ALL_KEY, 13, 5D);
        verify(zSetOperations).add(HOT_ALL_KEY, 17, 5D);
        verify(zSetOperations, never()).add(HOT_ALL_KEY, 25, 0D);
    }
}
