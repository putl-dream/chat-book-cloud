package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.utils.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 文章列表功能实现类，提供多种文章分页查询功能。
 * 功能包括但不限于：
 * - 获取最新文章列表
 * - 获取热门文章列表
 * - 根据分类/标签获取文章列表
 * - 搜索结果文章列表
 * - 系统每日推荐文章列表
 * - 个性化推荐文章列表
 * - 用户历史阅读文章列表
 * - 用户收藏文章列表
 * - 管理员审核文章列表
 * - 用户草稿箱文章列表
 *
 * @since 2025-01-13 20:46:01
 */
@Slf4j
@Service
public class ArticlePagePageServiceImpl extends BaseAbstractArticle implements ArticlePageService {

    /**
     * 获取最新文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getNewPage(Integer pageNo, Integer pageSize) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getStatus, 1).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取热门文章列表。
     * 返回最近30天内审核通过的文章，按创建时间倒序排列。
     *
     * 注意：当前实现基于时间维度的简化版本。
     * 完整的热门算法应综合考虑浏览量、点赞数、收藏数等因素，
     * 建议后续引入 Redis 缓存 + 定时任务计算热门度。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize) {
        // 获取最近30天内的已发布文章
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, 1)
                .ge(ArticleDO::getCreateTime, thirtyDaysAgo)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取今日热门文章列表。
     * 返回今天创建的审核通过的文章，按创建时间倒序排列。
     *
     * 注意：当前实现基于今日发布的文章。
     * 完整的今日热门算法应基于今日浏览量、点赞数等实时数据排序，
     * 建议后续引入 Redis 实时统计。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize) {
        // 获取今天开始时间
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, 1)
                .ge(ArticleDO::getCreateTime, todayStart)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 根据分类ID获取文章列表，按照创建时间倒序排列。
     *
     * @param pageNo    分页页码
     * @param pageSize  每页大小
     * @param category  分类ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getCategoryPage(Integer pageNo, Integer pageSize, Integer category) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getCategory, category).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 根据关键词搜索文章，匹配标题或摘要，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param like     搜索关键词
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getLikePage(Integer pageNo, Integer pageSize, String like) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().like(ArticleDO::getTitle, like).or().like(ArticleDO::getAbstractText, like).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取系统推荐文章列表。
     * 返回本周内审核通过的文章，按创建时间倒序排列。
     *
     * 注意：当前实现为降级策略，返回本周优质文章。
     * 完整的推荐算法应基于：
     * 1. 内容质量（点赞、收藏、阅读量综合评分）
     * 2. 发布时间衰减
     * 3. 分类多样性
     * 建议后续引入 Redis 缓存 + 定时任务计算推荐列表。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getSystemRecommendPage(Integer pageNo, Integer pageSize) {
        // 降级策略：返回所有审核通过的文章
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, 1)
//                .ge(ArticleDO::getCreateTime, weekStart)
//                .le(ArticleDO::getCreateTime, weekEnd)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取个性化推荐文章列表。
     * 基于用户兴趣推荐相关文章，当前返回最新发布的文章。
     *
     * 注意：当前实现为降级策略，返回最新文章。
     * 完整的个性化推荐算法应基于：
     * 1. 用户历史阅读行为分析
     * 2. 用户兴趣标签匹配
     * 3. 协同过滤算法
     * 建议后续引入机器学习模型 + 用户画像系统。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getPersonalRecommendPage(Integer pageNo, Integer pageSize) {
        // 降级策略：返回最近7天内的已发布文章
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getStatus, 1)
                .ge(ArticleDO::getCreateTime, sevenDaysAgo)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取用户历史阅读文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserHistoryPage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getUserId, userId).orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取用户收藏文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserCollectPage(Integer pageNo, Integer pageSize, Integer userId) {
        List<Integer> articleIdList = new ArrayList<>();
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .in(ArticleDO::getId, articleIdList)
                .orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取用户发布的文章列表，状态为已发布，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserArticlePage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, 1)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取用户草稿箱文章列表，状态为草稿，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @param userId   用户ID
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getUserDraftArticlePage(Integer pageNo, Integer pageSize, Integer userId) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .eq(ArticleDO::getUserId, userId)
                .eq(ArticleDO::getStatus, 0)
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取管理员审核文章列表，按照创建时间倒序排列。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getAdminArticlePage(Integer pageNo, Integer pageSize) {
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 根据ID列表查询文章列表
     *
     * @param ids 文章ID列表
     * @return 文章列表
     */
    @Override
    public List<ArticleListVO> selectIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        List<ArticleDO> articleDOS = articleMapper.selectBatchIds(ids);
        return toBean(articleDOS);
    }
}
