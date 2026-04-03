package com.putl.articleservice.controller;

import com.putl.articleservice.controller.vo.ArticleCommandResult;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "文章服务")
@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @Operation(summary = "保存草稿")
    @PostMapping("/saveDraft")
    public CommonResult<ArticleCommandResult> saveDraft(@RequestBody ArticleVO articleVO) {
        return CommonResult.success(articleService.saveDraft(articleVO));
    }

    @Operation(summary = "发布文章")
    @PostMapping("/publish")
    public CommonResult<ArticleCommandResult> publish(@RequestBody ArticleVO articleVO) {
        return CommonResult.success(articleService.publish(articleVO));
    }

    @Operation(summary = "兼容旧版添加文章接口")
    @PostMapping("/add")
    public CommonResult<ArticleCommandResult> addArticle(@RequestBody ArticleVO articleVO) {
        return CommonResult.success(articleService.saveDraft(articleVO));
    }

    @Operation(summary = "兼容旧版更新文章接口")
    @PostMapping("/update")
    public CommonResult<ArticleCommandResult> updateArticle(@RequestBody ArticleVO articleVO) {
        return CommonResult.success(articleService.saveDraft(articleVO));
    }

    @Operation(summary = "分页查询文章")
    @PostMapping("/queryPage")
    public CommonResult<PageResult<ArticleVO>> queryPage(
        @RequestParam Integer pageNum,
        @RequestParam Integer pageSize) {
        return CommonResult.success(articleService.queryPage(pageNum, pageSize));
    }

    @Operation(summary = "查询文章详情")
    @GetMapping("/query")
    public CommonResult<ArticleVO> getArticleInfo(@Parameter(description = "文章ID") @RequestParam("id") Integer id) {
        return CommonResult.success(articleService.getArticleInfo(id));
    }

    @Operation(summary = "删除文章")
    @DeleteMapping("/delete")
    public CommonResult<Void> deleteArticle(@Parameter(description = "文章ID") @RequestParam("id") Integer id) {
        articleService.deleteArticle(id);
        return CommonResult.success();
    }

    @Operation(summary = "查询文章总数")
    @GetMapping("/queryCount")
    public CommonResult<Long> queryCount() {
        return CommonResult.success(articleService.count());
    }

    @Operation(summary = "查询待审核文章总数")
    @GetMapping("/queryPendingReviewCount")
    public CommonResult<Long> queryPendingReviewCount() {
        return CommonResult.success(articleService.countPendingReview());
    }
}
