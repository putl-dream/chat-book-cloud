package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.UserFootDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [UserFoot]表数据库访问层  用户足迹表
 *
 * @since 2025-01-17 16:00:13
 */
@Mapper
public interface UserFootMapper extends BaseMapper<UserFootDO> {

}

