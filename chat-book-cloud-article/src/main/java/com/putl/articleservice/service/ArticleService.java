package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.utils.PageResult;

/**
 * 文章基础功能
 * - 文章详情
 * - 文章增删改查
 *
 * @since 2025-01-13 20:46:01
 */
public interface ArticleService {

    ArticleVO getArticleInfo(Integer articleId);

    ArticleVO getArticleDetail(Integer articleId);

    PageResult<ArticleVO> queryPage(Integer pageNum, Integer pageSize);

    void addArticle(ArticleVO articleVO);

    void updateArticle(ArticleVO articleVO);

    void deleteArticle(Integer articleId);

    void deleteArticleBatch(Integer[] articleIds);

    void updateArticleStatus(Integer articleId, ArticleStatus status);
}

