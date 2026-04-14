package com.putl.interactionservice.service.impl;

import com.putl.articleservice.api.ArticleClient;
import com.putl.interactionservice.controller.vo.UserFootListVO;
import com.putl.interactionservice.entity.ArticleStatDO;
import com.putl.interactionservice.mapper.ArticleStatMapper;
import com.putl.interactionservice.mapper.ReviewMapper;
import com.putl.interactionservice.mapper.UserFootMapper;
import com.putl.interactionservice.service.HotArticleRankService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserFootServiceImplTest {

    @Mock
    private UserFootMapper userFootMapper;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private ArticleStatMapper articleStatMapper;

    @Mock
    private ReviewMapper reviewMapper;

    @Mock
    private HotArticleRankService hotArticleRankService;

    @InjectMocks
    private UserFootServiceImpl userFootService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(userFootService, "baseMapper", userFootMapper);
    }

    @Test
    void getUserFootListByArticleIdsShouldBackfillMissingArticleStat() {
        Integer existingArticleId = 24;
        Integer missingArticleId = 25;
        ArticleStatDO existingStat = ArticleStatDO.builder()
                .articleId(existingArticleId)
                .viewCount(88L)
                .praiseCount(12L)
                .commentCount(6L)
                .collectCount(3L)
                .build();
        given(articleStatMapper.selectList(any())).willReturn(List.of(existingStat));
        given(articleStatMapper.selectOne(any())).willReturn(null);
        given(userFootMapper.selectCount(any())).willReturn(15L, 7L, 4L);
        given(reviewMapper.selectCount(any())).willReturn(5L);

        List<UserFootListVO> result = userFootService.getUserFootListByArticleIds(List.of(existingArticleId, missingArticleId));

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getArticleId()).isEqualTo(existingArticleId);
        assertThat(result.get(0).getViewCount()).isEqualTo(88L);
        assertThat(result.get(0).getPraiseCount()).isEqualTo(12L);
        assertThat(result.get(0).getCommentCount()).isEqualTo(6L);
        assertThat(result.get(0).getCollectCount()).isEqualTo(3L);

        assertThat(result.get(1).getArticleId()).isEqualTo(missingArticleId);
        assertThat(result.get(1).getViewCount()).isEqualTo(15L);
        assertThat(result.get(1).getPraiseCount()).isEqualTo(7L);
        assertThat(result.get(1).getCommentCount()).isEqualTo(5L);
        assertThat(result.get(1).getCollectCount()).isEqualTo(4L);

        ArgumentCaptor<ArticleStatDO> statCaptor = ArgumentCaptor.forClass(ArticleStatDO.class);
        verify(articleStatMapper).insert(statCaptor.capture());
        ArticleStatDO rebuiltStat = statCaptor.getValue();
        assertThat(rebuiltStat.getArticleId()).isEqualTo(missingArticleId);
        assertThat(rebuiltStat.getViewCount()).isEqualTo(15L);
        assertThat(rebuiltStat.getPraiseCount()).isEqualTo(7L);
        assertThat(rebuiltStat.getCommentCount()).isEqualTo(5L);
        assertThat(rebuiltStat.getCollectCount()).isEqualTo(4L);
    }
}
