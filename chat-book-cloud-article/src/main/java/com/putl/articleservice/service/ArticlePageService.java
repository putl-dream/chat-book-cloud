package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.utils.PageResult;

import java.util.List;

/**
 * 文章列表功能
 * - 获取最新文章列表
 * - 获取热门文章列表
 * - 分类/标签下的文章列表
 * - 搜索结果文章列表
 * - 系统每日推荐文章列表
 * - 个性化推荐文章列表
 * - 我的历史阅读列表
 * - 收藏文章列表
 * - 管理员审核文章列表
 * - 草稿箱文章列表
 *
 * @since 2025-01-13 20:46:01
 */
public interface ArticlePageService {

    // 获取最新文章列表
    PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize);

    // 获取热门文章列表
    PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize);

    // 获取今日热门文章列表
    PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize);

    // 分类/标签下的文章列表
    PageResult<ArticleListVO> getCategoryPage(Integer pageNo, Integer pageSize, Integer category);

    //  搜索结果文章列表
    PageResult<ArticleListVO> getLikePage(Integer pageNo, Integer pageSize, String like);

    // 系统每日推荐文章列表
    PageResult<ArticleListVO> getSystemRecommendPage(Integer pageNo, Integer pageSize);

    // 个性化推荐文章列表
    PageResult<ArticleListVO> getPersonalRecommendPage(Integer pageNo, Integer pageSize);

    // 我的历史阅读列表
    PageResult<ArticleListVO> getUserHistoryPage(Integer pageNo, Integer pageSize, Integer userId);

    // 收藏文章列表
    PageResult<ArticleListVO> getUserCollectPage(Integer pageNo, Integer pageSize, Integer userId);

    // 发表文章列表
    PageResult<ArticleListVO> getUserArticlePage(Integer pageNo, Integer pageSize, Integer userId);

    // 草稿箱文章列表
    PageResult<ArticleListVO> getUserDraftArticlePage(Integer pageNo, Integer pageSize, Integer userId);

    // 管理员审核文章列表
    PageResult<ArticleListVO> getAdminArticlePage(Integer pageNo, Integer pageSize);

    // 根据ID列表查询文章列表
    List<ArticleListVO> selectIds(List<Integer> ids);
}

