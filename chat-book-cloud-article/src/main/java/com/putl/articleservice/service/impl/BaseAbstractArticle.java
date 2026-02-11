package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.client.UserClient;
import com.putl.articleservice.client.result.UserFootVO;
import com.putl.articleservice.controller.vo.ArticleListVO;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

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
@Slf4j
public abstract class BaseAbstractArticle {

    @Resource
    protected ArticleMapper articleMapper;

    @Resource
    private UserClient userClient;

    protected PageResult<ArticleListVO> toBean(Integer pageNo, Integer pageSize, Wrapper<ArticleDO> wrapper) {
        PageResult<ArticleDO> articleDOPageResult = articleMapper.selectCustomizePage(new Page<>(pageNo, pageSize), wrapper);
        List<ArticleListVO> bean = BeanUtil.toBean(articleDOPageResult.getList(), ArticleListVO.class);
        PageResult<ArticleListVO> pageResult = new PageResult<>(bean, articleDOPageResult.getTotal());
        pageResult.getList().forEach(this::setArticleVO);
        return pageResult;
    }

    protected List<ArticleListVO> toBean(List<ArticleDO> articleDOs) {
        List<ArticleListVO> bean = BeanUtil.toBean(articleDOs, ArticleListVO.class);
        bean.forEach(this::setArticleVO);
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

    private void setArticleVO(ArticleListVO article) {
        try {
            // 调用带 userId 参数的方法，避免用户服务依赖 ReqInfoContext
            // 对于获取文章统计数据，当前用户 ID 不重要，传入 0 表示系统查询
            UserFootVO userFoot = userClient.getUserFoot(article.getId(), 0);
            if (userFoot != null) {
                article.setViewCount(userFoot.getViewCount());
                article.setPraiseCount(Long.valueOf(userFoot.getPraiseStat()));
                article.setCommentCount(Long.valueOf(userFoot.getCollectStat()));
            } else {
                // 设置默认值，避免空指针异常
                article.setViewCount(0L);
                article.setPraiseCount(0L);
                article.setCommentCount(0L);
                log.warn("UserFootVO is null for articleId: {}", article.getId());
            }
        } catch (Exception e) {
            // Feign 调用失败时设置默认值
            article.setViewCount(0L);
            article.setPraiseCount(0L);
            article.setCommentCount(0L);
            log.error("获取文章统计数据失败，articleId: {}", article.getId(), e);
        }
    }
}