package com.putl.articleservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.articleservice.mapper.entity.ArticleInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [ArticleInfo]表数据库访问层  文章详情表
 *
 * @since 2025-01-14 20:46:13
 */
@Mapper
public interface ArticleInfoMapper extends BaseMapper<ArticleInfoDO> {

}

