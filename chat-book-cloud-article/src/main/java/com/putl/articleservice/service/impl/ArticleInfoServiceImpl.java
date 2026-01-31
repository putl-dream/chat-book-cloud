package com.putl.articleservice.service.impl;


import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.articleservice.mapper.ArticleInfoMapper;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import com.putl.articleservice.service.ArticleInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * [ArticleInfo]表服务接口  文章详情表
 *
 * @since 2025-01-14 20:46:13
 */

@Service
@RequiredArgsConstructor
public class ArticleInfoServiceImpl extends ServiceImpl<ArticleInfoMapper, ArticleInfoDO> implements ArticleInfoService {

    private final ArticleInfoMapper articleInfoMapper;

    @Override
    @Cacheable(value = "articleInfo", key = "#articleId")
    public ArticleInfoDO getByArticleId(Integer articleId) {
        return articleInfoMapper.selectOne(Wrappers.<ArticleInfoDO>lambdaQuery().eq(ArticleInfoDO::getArticleId, articleId));
    }
}

