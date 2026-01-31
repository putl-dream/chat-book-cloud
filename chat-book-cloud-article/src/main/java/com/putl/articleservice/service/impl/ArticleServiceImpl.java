package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.mapper.ArticleInfoMapper;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.service.ArticleService;
import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 文章基础功能实现类
 * - 文章详情
 * - 文章增删改查
 *
 * @since 2025-01-13 20:46:01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl extends BaseAbstractArticle implements ArticleService {
    private final ArticleMapper articleMapper;
    private final ArticleInfoMapper articleInfoMapper;

    /**
     * 获取文章基本信息。
     *
     * @param articleId 文章ID
     * @return 包含文章基本信息的ArticleVO对象
     */
    @Override
    public ArticleVO getArticleInfo(Integer articleId) {
        return toBean(Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getId, articleId));
    }

    /**
     * 获取文章详细信息。
     *
     * @param articleId 文章ID
     * @return 包含文章详细信息的ArticleVO对象
     */
    @Override
    public ArticleVO getArticleDetail(Integer articleId) {
        return toBean(Wrappers.<ArticleDO>lambdaQuery().eq(ArticleDO::getId, articleId));
    }

    /**
     * 添加新文章。
     *
     * @param articleVO 包含新文章信息的ArticleVO对象
     * @return 包含新增文章信息的ArticleVO对象
     */
    @Override
    public void addArticle(ArticleVO articleVO) {
        ArticleDO articleDO = BeanUtil.toBean(articleVO, ArticleDO.class);
        articleMapper.insert(articleDO);
    }

    /**
     * 更新文章信息。
     *
     * @param articleVO 包含更新后文章信息的ArticleVO对象
     * @return 包含更新后文章信息的ArticleVO对象
     */
    @Override
    public void updateArticle(ArticleVO articleVO) {
        ArticleDO articleDO = BeanUtil.toBean(articleVO, ArticleDO.class);
        articleMapper.updateById(articleDO);
    }

    /**
     * 删除指定ID的文章。
     *
     * @param articleId 文章ID
     */
    @Override
    public void deleteArticle(Integer articleId) {
        articleMapper.deleteById(articleId);
    }

    /**
     * 批量删除指定ID的文章。
     *
     * @param articleIds 文章ID数组
     */
    @Override
    public void deleteArticleBatch(Integer[] articleIds) {
        articleMapper.deleteBatchIds(List.of(articleIds));
    }

    /**
     * 更新文章状态。
     *
     * @param articleId 文章ID
     * @param status    新的状态值
     */
    @Override
    public void updateArticleStatus(Integer articleId, Integer status) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO != null) {
            articleDO.setStatus(status);
            articleMapper.updateById(articleDO);
        }
    }
}
