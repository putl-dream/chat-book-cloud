package com.putl.articleservice.api.fallback;

import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class ArticleClientFallback implements FallbackFactory<ArticleClient> {

    @Override
    public ArticleClient create(Throwable cause) {
        log.error("[ArticleClient] Call failed, fallback enabled.", cause);

        return new ArticleClient() {
            @Override
            public CommonResult<ArticleVO> queryArticle(Integer id) {
                log.warn("[ArticleClient] queryArticle fallback, articleId: {}", id);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<List<ArticleListVO>> selectIds(List<Integer> ids) {
                log.warn("[ArticleClient] selectIds fallback, articleIds: {}", ids);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<Long> queryCount() {
                log.warn("[ArticleClient] queryCount fallback");
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<Long> queryPendingReviewCount() {
                log.warn("[ArticleClient] queryPendingReviewCount fallback");
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<CreateDraftResponse> createDraft(CreateDraftRequest request) {
                log.warn("[ArticleClient] createDraft fallback, request: {}", request);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<CreateDraftResponse> createDraftVersion(CreateDraftVersionRequest request) {
                log.warn("[ArticleClient] createDraftVersion fallback, request: {}", request);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<DraftDetailDTO> getDraftDetail(Integer draftId) {
                log.warn("[ArticleClient] getDraftDetail fallback, draftId: {}", draftId);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<Void> adoptDraftVersion(DraftVersionAdoptRequest request) {
                log.warn("[ArticleClient] adoptDraftVersion fallback, request: {}", request);
                return CommonResult.error(500, "Article service unavailable");
            }

            @Override
            public CommonResult<Void> evictHotPageCache() {
                log.warn("[ArticleClient] evictHotPageCache fallback");
                return CommonResult.error(500, "Article service unavailable");
            }
        };
    }
}
