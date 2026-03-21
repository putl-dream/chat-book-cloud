package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.interactionservice.api.InteractionClient;
import com.putl.interactionservice.api.dto.UserFootListVO;
import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserResult;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

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
@Slf4j
public abstract class BaseAbstractArticle {

    @Resource
    protected ArticleMapper articleMapper;

    @Resource
    private UserClient userClient;

    @Resource
    private InteractionClient interactionClient;

    protected PageResult<ArticleListVO> toBean(Integer pageNo, Integer pageSize, Wrapper<ArticleDO> wrapper) {
        PageResult<ArticleDO> articleDOPageResult = articleMapper.selectCustomizePage(new Page<>(pageNo, pageSize), wrapper);
        List<ArticleListVO> bean = BeanUtil.toBean(articleDOPageResult.getList(), ArticleListVO.class);
        PageResult<ArticleListVO> pageResult = new PageResult<>(bean, articleDOPageResult.getTotal());
        batchSetArticleVO(pageResult.getList());
        return pageResult;
    }

    protected List<ArticleListVO> toBean(List<ArticleDO> articleDOs) {
        List<ArticleListVO> bean = BeanUtil.toBean(articleDOs, ArticleListVO.class);
        batchSetArticleVO(bean);
        return bean;
    }

    protected ArticleVO toBean(Wrapper<ArticleDO> wrapper) {
        List<ArticleDO> articleList = articleMapper.selectList(wrapper);
        if (articleList.isEmpty()) {
            return null;
        }
        ArticleDO article = articleList.get(0);

        return BeanUtil.toBean(article, ArticleVO.class);
    }

    /**
     * 批量设置文章的用户信息和统计数据，减少 N+1 查询问题
     */
    private void batchSetArticleVO(List<ArticleListVO> articles) {
        if (articles == null || articles.isEmpty()) {
            return;
        }

        // 收集所有userId
        List<Integer> userIds = articles.stream()
                .map(ArticleListVO::getUserId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        // 收集所有articleId
        List<Integer> articleIds = articles.stream()
                .map(ArticleListVO::getId)
                .filter(id -> id != null)
                .distinct()
                .toList();

        // 批量查询用户信息（1次请求）
        Map<Integer, String> userAvatarMap = new HashMap<>();
        try {
            if (!userIds.isEmpty()) {
                CommonResult<List<UserResult>> userResult = userClient.getUsersByIds(userIds);
                if (userResult != null && userResult.getData() != null) {
                    for (UserResult user : userResult.getData()) {
                        if (user != null && user.getId() != null) {
                            userAvatarMap.put(user.getId(), user.getPhoto());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("批量获取用户头像失败, userIds: {}", userIds, e);
        }

        // 批量查询文章统计数据（1次请求）
        Map<Integer, UserFootListVO> footStatMap = new HashMap<>();
        try {
            if (!articleIds.isEmpty()) {
                CommonResult<List<UserFootListVO>> statResult = interactionClient.getUserFootListByArticleIds(articleIds);
                if (statResult != null && statResult.getData() != null) {
                    for (UserFootListVO stat : statResult.getData()) {
                        if (stat != null && stat.getArticleId() != null) {
                            footStatMap.put(stat.getArticleId(), stat);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("批量获取文章统计数据失败, articleIds: {}", articleIds, e);
        }

        // 设置到每个文章对象
        for (ArticleListVO article : articles) {
            // 设置作者头像
            if (article.getUserId() != null) {
                article.setAuthorAvatar(userAvatarMap.get(article.getUserId()));
            }

            // 设置统计数据
            UserFootListVO stat = footStatMap.get(article.getId());
            if (stat != null) {
                article.setViewCount(stat.getViewCount());
                article.setPraiseCount(stat.getPraiseCount());
                article.setCommentCount(stat.getCommentCount());
                article.setCollectCount(stat.getCollectCount());
            } else {
                article.setViewCount(0L);
                article.setPraiseCount(0L);
                article.setCommentCount(0L);
                article.setCollectCount(0L);
            }
        }
    }
}