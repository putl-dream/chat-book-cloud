package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.UserInfoDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [UserInfo]表数据库访问层  用户信息表
 *
 * @author putl
 * @since 2024-12-29 17:36:33
 */
@Mapper
public interface UserInfoMapper extends BaseMapper<UserInfoDO> {

}

