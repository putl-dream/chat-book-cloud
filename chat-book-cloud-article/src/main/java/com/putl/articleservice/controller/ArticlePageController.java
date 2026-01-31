package com.putl.articleservice.controller;

import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/page")
@RequiredArgsConstructor
@Tag(name = "文章列表接口")
public class ArticlePageController {

    private final ArticlePageService articlePageService;

    @Operation(summary = "获取最新文章列表")
    @PostMapping("/newPage")
    public CommonResult<PageResult<ArticleListVO>> getNewPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getNewPage(pageNo, pageSize));
    }

    @Operation(summary = "获取热门文章列表")
    @PostMapping("/hotPage")
    public CommonResult<PageResult<ArticleListVO>> getHotPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getHotPage(pageNo, pageSize));
    }

    @Operation(summary = "获取今日热门文章列表")
    @PostMapping("/todayHotPage")
    public CommonResult<PageResult<ArticleListVO>> getTodayHotPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getTodayHotPage(pageNo, pageSize));
    }

    @Operation(summary = "根据分类获取文章列表")
    @PostMapping("/categoryPage")
    public CommonResult<PageResult<ArticleListVO>> getCategoryPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam Integer category) {
        return CommonResult.success(articlePageService.getCategoryPage(pageNo, pageSize, category));
    }

    @Operation(summary = "根据关键词搜索文章")
    @PostMapping("/likePage")
    public CommonResult<PageResult<ArticleListVO>> getLikePage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam String like) {
        return CommonResult.success(articlePageService.getLikePage(pageNo, pageSize, like));
    }

    @Operation(summary = "获取系统推荐文章列表")
    @PostMapping("/systemRecommendPage")
    public CommonResult<PageResult<ArticleListVO>> getSystemRecommendPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getSystemRecommendPage(pageNo, pageSize));
    }

    @Operation(summary = "获取个性化推荐文章列表")
    @PostMapping("/personalRecommendPage")
    public CommonResult<PageResult<ArticleListVO>> getPersonalRecommendPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getPersonalRecommendPage(pageNo, pageSize));
    }

    @Operation(summary = "获取用户历史阅读文章列表")
    @PostMapping("/userHistoryPage")
    public CommonResult<PageResult<ArticleListVO>> getUserHistoryPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam Integer userId) {
        return CommonResult.success(articlePageService.getUserHistoryPage(pageNo, pageSize, userId));
    }

    @Operation(summary = "获取用户收藏文章列表")
    @PostMapping("/userCollectPage")
    public CommonResult<PageResult<ArticleListVO>> getUserCollectPage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam Integer userId) {
        return CommonResult.success(articlePageService.getUserCollectPage(pageNo, pageSize, userId));
    }

    @Operation(summary = "获取用户发布的文章列表")
    @PostMapping("/userArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getUserArticlePage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam Integer userId) {
        return CommonResult.success(articlePageService.getUserArticlePage(pageNo, pageSize, userId));
    }

    @Operation(summary = "获取用户草稿箱文章列表")
    @PostMapping("/userDraftArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getUserDraftArticlePage(@RequestParam Integer pageNo, @RequestParam Integer pageSize, @RequestParam Integer userId) {
        return CommonResult.success(articlePageService.getUserDraftArticlePage(pageNo, pageSize, userId));
    }

    @Operation(summary = "获取管理员审核文章列表")
    @PostMapping("/adminArticlePage")
    public CommonResult<PageResult<ArticleListVO>> getAdminArticlePage(@RequestParam Integer pageNo, @RequestParam Integer pageSize) {
        return CommonResult.success(articlePageService.getAdminArticlePage(pageNo, pageSize));
    }
}