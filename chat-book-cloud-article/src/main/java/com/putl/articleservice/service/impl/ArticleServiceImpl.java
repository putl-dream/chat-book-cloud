package com.putl.articleservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.enums.ArticleStatus;
import com.putl.articleservice.mapper.ArticleInfoMapper;
import com.putl.articleservice.mapper.ArticleMapper;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.context.UserContext;
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
    private final com.putl.articleservice.client.UserClient userClient;

    /**
     * 获取文章基本信息。
     *
     * @param articleId 文章ID
     * @return 包含文章基本信息的ArticleVO对象
     */
    @Override
    public ArticleVO getArticleInfo(Integer articleId) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO == null) {
            return null;
        }
        ArticleVO articleVO = BeanUtil.toBean(articleDO, ArticleVO.class);

        // 填充文章详情内容
        ArticleInfoDO articleInfoDO = articleInfoMapper.selectOne(Wrappers.<ArticleInfoDO>lambdaQuery()
                .eq(ArticleInfoDO::getArticleId, articleId));
        if (articleInfoDO != null) {
            articleVO.setContent(articleInfoDO.getContent());
            if ((articleVO.getUserName() == null || articleVO.getUserName().isEmpty()) && articleInfoDO.getUserName() != null) {
                articleVO.setUserName(articleInfoDO.getUserName());
            }
        }

        // 填充统计数据
        try {
            String currentUserIdStr = UserContext.getUserId();
            Integer currentUserId = currentUserIdStr != null ? Integer.valueOf(currentUserIdStr) : 0;
            
            com.putl.articleservice.client.result.UserFootVO userFoot = userClient.getUserFoot(articleId, currentUserId);
            if (userFoot != null) {
                articleVO.setViewCount(userFoot.getViewCount());
                articleVO.setPraiseStat(userFoot.getPraiseStat() != null ? userFoot.getPraiseStat().intValue() : 0);
                articleVO.setCollectStat(userFoot.getCollectStat() != null ? userFoot.getCollectStat().intValue() : 0);
            }
        } catch (Exception e) {
            log.error("获取用户足迹信息失败: articleId={}", articleId, e);
        }

        return articleVO;
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
     * 分页查询文章。
     *
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    @Override
    public PageResult<ArticleVO> queryPage(Integer pageNum, Integer pageSize) {
        Page<ArticleDO> page = new Page<>(pageNum, pageSize);
        Page<ArticleDO> articleDOPage = articleMapper.selectPage(page, Wrappers.emptyWrapper());
        List<ArticleVO> articleVOS = BeanUtil.toBean(articleDOPage.getRecords(), ArticleVO.class);
        return new PageResult<>(articleVOS, articleDOPage.getTotal());
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
        String userId = UserContext.getUserId();
        if (userId != null) {
            articleDO.setUserId(Integer.valueOf(userId));
        }
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
        // 更新时也可以校验一下权限，或者确保 userId 不被串改
        String userId = UserContext.getUserId();
        if (userId != null) {
            articleDO.setUserId(Integer.valueOf(userId));
        }
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
    public void updateArticleStatus(Integer articleId, ArticleStatus status) {
        ArticleDO articleDO = articleMapper.selectById(articleId);
        if (articleDO != null) {
            articleDO.setStatus(status);
            articleMapper.updateById(articleDO);
        }
    }
}
