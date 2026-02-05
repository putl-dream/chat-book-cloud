package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.CategoryPageRequestDTO;
import com.putl.articleservice.controller.dto.PageRequestDTO;
import com.putl.articleservice.controller.dto.SearchPageRequestDTO;
import com.putl.articleservice.controller.dto.UserPageRequestDTO;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page")
@RequiredArgsConstructor
@Tag(name = "文章列表接口")
public class ArticlePageController {

    private final ArticlePageService articlePageService;

    @Operation(summary = "获取最新文章列表")
    @PostMapping("/newPage")
    public CommonResult<PageResult<ArticleListVO>> getNewPage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getNewPage(request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "获取热门文章列表")
    @PostMapping("/hotPage")
    public CommonResult<PageResult<ArticleListVO>> getHotPage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getHotPage(request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "获取今日热门文章列表")
    @PostMapping("/todayHotPage")
    public CommonResult<PageResult<ArticleListVO>> getTodayHotPage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getTodayHotPage(request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "根据分类获取文章列表")
    @PostMapping("/categoryPage")
    public CommonResult<PageResult<ArticleListVO>> getCategoryPage(@Valid @RequestBody CategoryPageRequestDTO request) {
        return CommonResult.success(articlePageService.getCategoryPage(request.getPageNo(), request.getPageSize(), request.getCategory()));
    }

    @Operation(summary = "根据关键词搜索文章")
    @PostMapping("/likePage")
    public CommonResult<PageResult<ArticleListVO>> getLikePage(@Valid @RequestBody SearchPageRequestDTO request) {
        return CommonResult.success(articlePageService.getLikePage(request.getPageNo(), request.getPageSize(), request.getKeyword()));
    }

    @Operation(summary = "获取系统推荐文章列表")
    @PostMapping("/systemRecommendPage")
    public CommonResult<PageResult<ArticleListVO>> getSystemRecommendPage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getSystemRecommendPage(request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "获取个性化推荐文章列表")
    @PostMapping("/personalRecommendPage")
    public CommonResult<PageResult<ArticleListVO>> getPersonalRecommendPage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getPersonalRecommendPage(request.getPageNo(), request.getPageSize()));
    }

    @Operation(summary = "获取用户历史阅读文章列表")
    @PostMapping("/userHistoryPage")
    public CommonResult<PageResult<ArticleListVO>> getUserHistoryPage(@Valid @RequestBody PageRequestDTO request) {
        Integer userId = Integer.valueOf(UserContext.getUserId());
        return CommonResult.success(articlePageService.getUserHistoryPage(request.getPageNo(), request.getPageSize(), userId));
    }

    @Operation(summary = "获取用户收藏文章列表")
    @PostMapping("/userCollectPage")
    public CommonResult<PageResult<ArticleListVO>> getUserCollectPage(@Valid @RequestBody PageRequestDTO request) {
        Integer userId = Integer.valueOf(UserContext.getUserId());
        return CommonResult.success(articlePageService.getUserCollectPage(request.getPageNo(), request.getPageSize(), userId));
    }

    @Operation(summary = "获取用户发布的文章列表")
    @PostMapping("/userArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getUserArticlePage(@Valid @RequestBody UserPageRequestDTO request) {
        Integer userId = request.getUserId();
        if (userId == null) {
            String currentUserId = UserContext.getUserId();
            if (currentUserId != null) {
                userId = Integer.valueOf(currentUserId);
            } else {
                 throw new IllegalArgumentException("用户ID不能为空");
            }
        }
        return CommonResult.success(articlePageService.getUserArticlePage(request.getPageNo(), request.getPageSize(), userId));
    }

    @Operation(summary = "获取用户草稿箱文章列表")
    @PostMapping("/userDraftArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getUserDraftArticlePage(@Valid @RequestBody PageRequestDTO request) {
        Integer userId = Integer.valueOf(UserContext.getUserId());
        return CommonResult.success(articlePageService.getUserDraftArticlePage(request.getPageNo(), request.getPageSize(), userId));
    }

    @Operation(summary = "获取管理员审核文章列表")
    @PostMapping("/adminArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getAdminArticlePage(@Valid @RequestBody PageRequestDTO request) {
        return CommonResult.success(articlePageService.getAdminArticlePage(request.getPageNo(), request.getPageSize()));
    }
}
