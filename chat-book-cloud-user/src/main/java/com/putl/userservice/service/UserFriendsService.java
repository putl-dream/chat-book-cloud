package com.putl.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.common.enums.FriendRelationEnum;
import com.putl.userservice.mapper.entity.UserFriendsDO;

import java.util.List;

/**
 * [UserFriends]表服务接口
 *
 * @since 2025-01-15 16:49:34
 */
public interface UserFriendsService extends IService<UserFriendsDO> {
    /**
     * 添加好友
     * <p>1、检查好友关系是否存在</p>
     * <p>2、若好友功能不存在，添加好友</p>
     */
    String addFriend(Integer userId, Integer friendId);

    /**
     * 检查好友关系是否存在关注可能
     * <p>1、查询本人是否关注对方</p>
     * <p>2、查询对方是否关注本人</p>
     * <p>3、若存在关系，返回关系id</p>
     */
    FriendRelationEnum checkFriendRelation(Integer userId, Integer friendId);

    /**
     * 查询好友关系
     */
    List<Integer> getFriendList(Integer userId);

}

