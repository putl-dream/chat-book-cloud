package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.utils.PageResult;

/**
 * 文章列表首页缓存服务
 * Phase 1 只缓存第一页，5分钟 TTL
 */
public interface ArticleListCacheService {

    /**
     * 获取最新文章列表（仅第一页缓存）
     */
    PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize);

    /**
     * 获取热门文章列表（仅第一页缓存）
     */
    PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize);

    /**
     * 获取今日热门文章列表（仅第一页缓存）
     */
    PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize);

    /**
     * 获取标签下的文章列表（仅第一页缓存）
     */
    PageResult<ArticleListVO> getTagPage(Integer pageNo, Integer pageSize, Integer tagId);

    /**
     * 失效首页最新文章缓存
     */
    void evictNewCache();

    /**
     * 失效首页热门文章缓存
     */
    void evictHotCache();

    /**
     * 失效今日热门文章缓存
     */
    void evictTodayHotCache();
}