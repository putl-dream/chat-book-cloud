package com.putl.articleservice.service;

import com.putl.articleservice.controller.vo.ArticleCommandResult;
import com.putl.articleservice.controller.vo.ArticleReviewResultVO;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.enums.ArticleReviewAction;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.utils.PageResult;

import java.util.List;

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

    ArticleCommandResult saveDraft(ArticleVO articleVO);

    ArticleCommandResult publish(ArticleVO articleVO);

    PageResult<ArticleVO> queryPage(Integer pageNum, Integer pageSize);

    void addArticle(ArticleVO articleVO);

    void updateArticle(ArticleVO articleVO);

    void deleteArticle(Integer articleId);

    void deleteArticleBatch(Integer[] articleIds);

    void updateArticleStatus(Integer articleId, ArticleStatus status);

    ArticleReviewResultVO approveArticle(Integer articleId);

    ArticleReviewResultVO rejectArticle(Integer articleId, String reason);

    List<ArticleReviewResultVO> batchReviewArticles(List<Integer> articleIds, ArticleReviewAction action, String reason);

    // ==================== 后台治理动作 ====================

    /** 管理员上架文章 */
    void publishArticle(Integer articleId);

    /** 管理员下架文章 */
    void unpublishArticle(Integer articleId);

    /** 管理员删除文章 */
    void adminDeleteArticle(Integer articleId);

    /** 管理员恢复文章 */
    void restoreArticle(Integer articleId);

    /** 批量上架 */
    void batchPublish(List<Integer> articleIds);

    /** 批量下架 */
    void batchUnpublish(List<Integer> articleIds);

    /** 批量删除 */
    void batchDelete(List<Integer> articleIds);

    /** 查询文章总数 */
    Long count();

    /** 查询待审核文章总数 */
    Long countPendingReview();
}

