package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.AdminArticlePageRequestDTO;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 后台文章内容治理接口
 */
@Tag(name = "后台文章内容治理接口")
@RestController
@RequestMapping("/article/admin")
@RequiredArgsConstructor
@Validated
@RequireAdmin
public class ArticleContentAdminController {

    private final ArticlePageService articlePageService;
    private final ArticleService articleService;

    @Operation(summary = "全站文章分页查询（管理员）")
    @GetMapping("/page")
    public CommonResult<PageResult<ArticleListVO>> getAdminFullPage(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer category,
            @RequestParam(required = false) Integer contentType,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "desc") String orderDirection) {
        AdminArticlePageRequestDTO request = AdminArticlePageRequestDTO.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .status(status)
                .category(category)
                .contentType(contentType)
                .userId(userId)
                .keyword(keyword)
                .orderDirection(orderDirection)
                .build();
        return CommonResult.success(articlePageService.getAdminFullPage(request));
    }

    // ==================== 内容治理动作 ====================

    @Operation(summary = "文章上架")
    @PutMapping("/{articleId}/publish")
    public CommonResult<Void> publish(@PathVariable Integer articleId) {
        articleService.publishArticle(articleId);
        return CommonResult.success();
    }

    @Operation(summary = "文章下架")
    @PutMapping("/{articleId}/unpublish")
    public CommonResult<Void> unpublish(@PathVariable Integer articleId) {
        articleService.unpublishArticle(articleId);
        return CommonResult.success();
    }

    @Operation(summary = "删除文章（管理员）")
    @DeleteMapping("/{articleId}")
    public CommonResult<Void> delete(@PathVariable Integer articleId) {
        articleService.adminDeleteArticle(articleId);
        return CommonResult.success();
    }

    @Operation(summary = "恢复文章")
    @PutMapping("/{articleId}/restore")
    public CommonResult<Void> restore(@PathVariable Integer articleId) {
        articleService.restoreArticle(articleId);
        return CommonResult.success();
    }

    // ==================== 批量操作 ====================

    @Operation(summary = "批量上架")
    @PutMapping("/batch/publish")
    public CommonResult<Void> batchPublish(@RequestBody @NotEmpty List<Integer> articleIds) {
        articleService.batchPublish(articleIds);
        return CommonResult.success();
    }

    @Operation(summary = "批量下架")
    @PutMapping("/batch/unpublish")
    public CommonResult<Void> batchUnpublish(@RequestBody @NotEmpty List<Integer> articleIds) {
        articleService.batchUnpublish(articleIds);
        return CommonResult.success();
    }

    @Operation(summary = "批量删除")
    @DeleteMapping("/batch")
    public CommonResult<Void> batchDelete(@RequestBody @NotEmpty List<Integer> articleIds) {
        articleService.batchDelete(articleIds);
        return CommonResult.success();
    }
}
