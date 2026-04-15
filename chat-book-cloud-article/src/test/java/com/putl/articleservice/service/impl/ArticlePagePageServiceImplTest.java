package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.utils.PageResult;
import com.putl.interactionservice.api.InteractionClient;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArticlePagePageServiceImplTest {

    private static final String ENV = "dev";
    private static final String HOT_ALL_KEY = "cbc:dev:interaction:hot:all";
    private static final String HOT_DAY_KEY = "cbc:dev:interaction:hot:day:" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);

    @Mock
    private ArticleMapper articleMapper;

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ZSetOperations<String, Object> zSetOperations;

    @Mock
    private InteractionClient interactionClient;

    private ArticlePagePageServiceImpl service;

    @BeforeEach
    void setUp() {
        service = spy(new ArticlePagePageServiceImpl());
        ReflectionTestUtils.setField(service, "articleMapper", articleMapper);
        ReflectionTestUtils.setField(service, "redisTemplate", redisTemplate);
        ReflectionTestUtils.setField(service, "interactionClient", interactionClient);
        ReflectionTestUtils.setField(service, "env", ENV);

        when(redisTemplate.opsForZSet()).thenReturn(zSetOperations);

        doAnswer(invocation -> {
            List<ArticleDO> articles = invocation.getArgument(0);
            return articles.stream()
                .map(article -> ArticleListVO.builder().id(article.getId()).build())
                .toList();
        }).when(service).toBean(anyList());
    }

    @Test
    void getHotPageShouldKeepRedisRankOrderWithoutFillingLatestArticles() {
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(3L, 3L);
        when(zSetOperations.reverseRange(HOT_ALL_KEY, 0L, 14L)).thenReturn(linkedSet(13, 17, 25));
        when(articleMapper.selectBatchIds(anyList())).thenReturn(List.of(
            article(13, ArticleStatus.PUBLISHED),
            article(17, ArticleStatus.PUBLISHED),
            article(25, ArticleStatus.PUBLISHED)
        ));

        PageResult<ArticleListVO> result = service.getHotPage(1, 5);

        assertThat(result.getList()).extracting(ArticleListVO::getId)
            .containsExactly(13, 17, 25);
        assertThat(result.getTotal()).isEqualTo(3L);
        verify(service, never()).toBean(eq(1), eq(5), any(Wrapper.class));
    }

    @Test
    void getHotPageShouldDropInvalidRankMembersInsteadOfMergingFallbackArticles() {
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(3L, 1L);
        when(zSetOperations.reverseRange(HOT_ALL_KEY, 0L, 14L)).thenReturn(linkedSet(13, 17, 25));
        when(articleMapper.selectBatchIds(anyList())).thenReturn(List.of(
            article(13, ArticleStatus.DRAFT),
            article(17, ArticleStatus.DELETED),
            article(25, ArticleStatus.PUBLISHED)
        ));

        PageResult<ArticleListVO> result = service.getHotPage(1, 5);

        assertThat(result.getList()).extracting(ArticleListVO::getId)
            .containsExactly(25);
        assertThat(result.getTotal()).isEqualTo(1L);
        verify(zSetOperations).remove(HOT_ALL_KEY, 13);
        verify(zSetOperations).remove(HOT_ALL_KEY, 17);
        verify(service, never()).toBean(eq(1), eq(5), any(Wrapper.class));
    }

    @Test
    void getHotPageShouldTriggerLazyInitializationWhenGlobalRankIsEmpty() {
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(0L, 2L, 2L);
        when(interactionClient.initializeAllHotRankIfAbsent()).thenReturn(CommonResult.success(2L));
        when(zSetOperations.reverseRange(HOT_ALL_KEY, 0L, 14L)).thenReturn(linkedSet(13, 17));
        when(articleMapper.selectBatchIds(anyList())).thenReturn(List.of(
            article(13, ArticleStatus.PUBLISHED),
            article(17, ArticleStatus.PUBLISHED)
        ));

        PageResult<ArticleListVO> result = service.getHotPage(1, 5);

        assertThat(result.getList()).extracting(ArticleListVO::getId)
            .containsExactly(13, 17);
        assertThat(result.getTotal()).isEqualTo(2L);
        verify(interactionClient).initializeAllHotRankIfAbsent();
    }

    @Test
    void getTodayHotPageShouldPrioritizeDayRankAndFillFromOverallRank() {
        when(zSetOperations.zCard(HOT_DAY_KEY)).thenReturn(2L, 2L);
        when(zSetOperations.zCard(HOT_ALL_KEY)).thenReturn(5L, 5L);
        when(zSetOperations.reverseRange(HOT_DAY_KEY, 0L, 14L)).thenReturn(linkedSet(13, 17));
        when(zSetOperations.reverseRange(HOT_ALL_KEY, 0L, 14L)).thenReturn(linkedSet(17, 25, 24, 23));
        when(articleMapper.selectBatchIds(anyList())).thenReturn(
            List.of(
                article(13, ArticleStatus.PUBLISHED),
                article(17, ArticleStatus.PUBLISHED)
            ),
            List.of(
                article(17, ArticleStatus.PUBLISHED),
                article(25, ArticleStatus.PUBLISHED),
                article(24, ArticleStatus.PUBLISHED),
                article(23, ArticleStatus.PUBLISHED)
            )
        );

        PageResult<ArticleListVO> result = service.getTodayHotPage(1, 5);

        assertThat(result.getList()).extracting(ArticleListVO::getId)
            .containsExactly(13, 17, 25, 24, 23);
        assertThat(result.getTotal()).isEqualTo(5L);
        verify(service, never()).toBean(eq(1), eq(5), any(Wrapper.class));
    }

    private static ArticleDO article(Integer id, ArticleStatus status) {
        return ArticleDO.builder()
            .id(id)
            .status(status)
            .build();
    }

    private static Set<Object> linkedSet(Object... values) {
        return new LinkedHashSet<>(Arrays.asList(values));
    }
}
