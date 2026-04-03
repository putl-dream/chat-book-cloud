package com.putl.userservice.controller;

import com.putl.articleservice.api.ArticleClient;
import com.putl.userservice.controller.vo.DataCount;
import com.putl.userservice.mapper.AdminOperationLogMapper;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class AdminControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private ArticleClient articleClient;

    @Mock
    private AdminOperationLogMapper adminOperationLogMapper;

    @InjectMocks
    private AdminController adminController;

    @Test
    void getDataCountShouldAggregateUserAndArticleCounts() {
        given(userService.count()).willReturn(12L);
        given(articleClient.queryCount()).willReturn(CommonResult.success(34L));

        CommonResult<DataCount> result = adminController.getDataCount();

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getUserCount()).isEqualTo(12L);
        assertThat(result.getData().getArticleCount()).isEqualTo(34L);
        assertThat(result.getData().getReviewCount()).isEqualTo(0L);
    }

    @Test
    void getDataCountShouldFallbackToZeroWhenArticleCountPayloadIsEmpty() {
        given(userService.count()).willReturn(12L);
        given(articleClient.queryCount()).willReturn(CommonResult.success());

        CommonResult<DataCount> result = adminController.getDataCount();

        assertThat(result.getData()).isNotNull();
        assertThat(result.getData().getArticleCount()).isEqualTo(0L);
    }
}
