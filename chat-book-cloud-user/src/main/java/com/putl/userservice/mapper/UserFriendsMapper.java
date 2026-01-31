package com.putl.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.putl.userservice.mapper.entity.UserFriendsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * [UserFriends]表数据库访问层
 *
 * @since 2025-01-15 16:49:34
 */
@Mapper
public interface UserFriendsMapper extends BaseMapper<UserFriendsDO> {

}

