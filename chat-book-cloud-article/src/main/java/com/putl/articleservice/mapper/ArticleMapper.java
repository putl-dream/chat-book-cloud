package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.mapper.entity.ArticleDO;
import com.putl.articleservice.utils.PageResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * [Article]表数据库访问层  文章表
 *
 * @since 2025-01-13 20:46:01
 */
@Mapper
public interface ArticleMapper extends BaseMapper<ArticleDO> {

    default PageResult<ArticleDO> selectCustomizePage(Page<ArticleDO> page, @Param(Constants.WRAPPER) Wrapper<ArticleDO> queryWrapper) {
        Page<ArticleDO> pageDO = selectPage(page, queryWrapper);
        return new PageResult<>(pageDO.getRecords(), pageDO.getTotal());
    }
}

