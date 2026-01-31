package com.putl.articleservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * [ArticleInfo]表服务接口  文章详情表
 *
 * @since 2025-01-14 20:46:13
 */
public interface ArticleInfoService extends IService<ArticleInfoDO> {

    ArticleInfoDO getByArticleId(@Valid @NotNull Integer articleId);
}

