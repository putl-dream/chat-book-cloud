package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.service.ArticlePageService;
import com.putl.articleservice.utils.PageResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery().orderByDesc(ArticleDO::getCreateTime));
    }

    /**
     * 获取热门文章列表（未实现）。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getHotPage(Integer pageNo, Integer pageSize) {
        return null;
    }

    /**
     * 获取今日热门文章列表（未实现）。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getTodayHotPage(Integer pageNo, Integer pageSize) {
        return null;
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
     * 获取系统推荐文章列表，限制时间为本周，状态为已发布。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getSystemRecommendPage(Integer pageNo, Integer pageSize) {
        // todo list需要使用缓存
        List<Integer> list = new ArrayList<>();
        return toBean(0, 10, Wrappers.<ArticleDO>lambdaQuery()
                .in(ArticleDO::getId, list)
                .eq(ArticleDO::getStatus, 1)
                // 时间范围在本周
                .ge(ArticleDO::getCreateTime, LocalDateTime.now().with(DayOfWeek.MONDAY))
                .le(ArticleDO::getCreateTime, LocalDateTime.now().with(DayOfWeek.SUNDAY))
                .orderByDesc(ArticleDO::getCreateTime)
        );
    }

    /**
     * 获取个性化推荐文章列表，状态为已发布。
     *
     * @param pageNo   分页页码
     * @param pageSize 每页大小
     * @return 包含文章数据的分页结果
     */
    @Override
    public PageResult<ArticleListVO> getPersonalRecommendPage(Integer pageNo, Integer pageSize) {
        List<Integer> list = new ArrayList<>();
        return toBean(pageNo, pageSize, Wrappers.<ArticleDO>lambdaQuery()
                .in(ArticleDO::getId, list)
                .eq(ArticleDO::getStatus, 1)
                // 时间范围在本周
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
                .eq(ArticleDO::getId, userId)
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
}
