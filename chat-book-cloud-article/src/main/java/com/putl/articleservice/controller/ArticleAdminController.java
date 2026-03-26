package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.AdminArticleBatchReviewRequestDTO;
import com.putl.articleservice.controller.dto.AdminArticleReviewRequestDTO;
import com.putl.articleservice.controller.vo.ArticleReviewResultVO;
import com.putl.articleservice.service.ArticleService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "后台文章治理接口")
@RestController
@RequestMapping("/article/admin/review")
@RequiredArgsConstructor
public class ArticleAdminController {

    private final ArticleService articleService;

    @Operation(summary = "管理员审核通过文章")
    @PostMapping("/approve")
    @RequireAdmin
    public CommonResult<ArticleReviewResultVO> approve(@Valid @RequestBody AdminArticleReviewRequestDTO request) {
        return CommonResult.success(articleService.approveArticle(request.getArticleId()));
    }

    @Operation(summary = "管理员驳回文章")
    @PostMapping("/reject")
    @RequireAdmin
    public CommonResult<ArticleReviewResultVO> reject(@Valid @RequestBody AdminArticleReviewRequestDTO request) {
        return CommonResult.success(articleService.rejectArticle(request.getArticleId(), request.getReason()));
    }

    @Operation(summary = "管理员批量审核文章")
    @PostMapping("/batch")
    @RequireAdmin
    public CommonResult<List<ArticleReviewResultVO>> batchReview(@Valid @RequestBody AdminArticleBatchReviewRequestDTO request) {
        return CommonResult.success(articleService.batchReviewArticles(
                request.getArticleIds(),
                request.getAction(),
                request.getReason()));
    }
}
