package com.putl.interactionservice.api.fallback;

import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.interactionservice.api.InteractionClient;
import com.putl.interactionservice.api.dto.NotificationVO;
import com.putl.interactionservice.api.dto.ReviewListVO;
import com.putl.interactionservice.api.dto.ReviewVO;
import com.putl.interactionservice.api.dto.UserFootListVO;
import com.putl.interactionservice.api.dto.UserFootVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class InteractionClientFallback implements FallbackFactory<InteractionClient> {

    @Override
    public InteractionClient create(Throwable cause) {
        log.error("[InteractionClient] Call failed, fallback enabled.", cause);

        return new InteractionClient() {
            @Override
            public boolean addBrowse(Integer articleId, Integer userId) {
                log.warn("[InteractionClient] addBrowse fallback, articleId: {}, userId: {}", articleId, userId);
                return false;
            }

            @Override
            public CommonResult<Integer> updatePraise(Integer articleId) {
                log.warn("[InteractionClient] updatePraise fallback, articleId: {}", articleId);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<Integer> updateCollection(Integer articleId) {
                log.warn("[InteractionClient] updateCollection fallback, articleId: {}", articleId);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<Integer> updateComment(Integer articleId) {
                log.warn("[InteractionClient] updateComment fallback, articleId: {}", articleId);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public UserFootVO getUserFoot(Integer articleId, Integer userId) {
                log.warn("[InteractionClient] getUserFoot fallback, articleId: {}, userId: {}", articleId, userId);
                return null;
            }

            @Override
            public UserFootListVO getUserFootList(Integer articleId) {
                log.warn("[InteractionClient] getUserFootList fallback, articleId: {}", articleId);
                return null;
            }

            @Override
            public CommonResult<List<UserFootListVO>> getUserFootListByArticleIds(List<Integer> articleIds) {
                log.warn("[InteractionClient] getUserFootListByArticleIds fallback, articleIds: {}", articleIds);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<Long> initializeAllHotRankIfAbsent() {
                log.warn("[InteractionClient] initializeAllHotRankIfAbsent fallback");
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<List<ArticleListVO>> getHistory(Integer page, Integer size) {
                log.warn("[InteractionClient] getHistory fallback, page: {}, size: {}", page, size);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<List<NotificationVO>> getNotifications() {
                log.warn("[InteractionClient] getNotifications fallback");
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<List<ReviewListVO>> getReviewsByArticleId(Integer articleId) {
                log.warn("[InteractionClient] getReviewsByArticleId fallback, articleId: {}", articleId);
                return CommonResult.error(500, "Interaction service unavailable");
            }

            @Override
            public CommonResult<String> saveReview(ReviewVO reviewVO) {
                log.warn("[InteractionClient] saveReview fallback, reviewVO: {}", reviewVO);
                return CommonResult.error(500, "Interaction service unavailable");
            }
        };
    }
}
