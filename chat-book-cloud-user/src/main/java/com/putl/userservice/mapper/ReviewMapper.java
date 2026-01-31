package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.ReviewDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [Review]表数据库访问层  评论表
 *
 * @since 2025-01-18 12:03:23
 */
@Mapper
public interface ReviewMapper extends BaseMapper<ReviewDO> {

}

