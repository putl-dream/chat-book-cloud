package com.putl.socialservice.service;

import com.putl.socialservice.enums.FriendRelationEnum;
import com.putl.socialservice.vo.FollowStatVO;

import java.util.List;

/**
 * 用户关注服务接口
 */
public interface UserFollowService {

    /**
     * 关注用户
     * @param userId 关注者用户ID
     * @param followId 被关注者用户ID
     * @return 操作结果
     */
    String follow(Integer userId, Integer followId);

    /**
     * 取消关注
     * @param userId 关注者用户ID
     * @param followId 被关注者用户ID
     * @return 操作结果
     */
    String unfollow(Integer userId, Integer followId);

    /**
     * 检查好友关系
     * @param userId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 关系枚举
     */
    FriendRelationEnum checkFriendRelation(Integer userId, Integer targetUserId);

    /**
     * 获取关注列表
     * @param userId 用户ID
     * @return 关注用户ID列表
     */
    List<Integer> getFollowList(Integer userId);

    /**
     * 获取粉丝列表
     * @param userId 用户ID
     * @return 粉丝用户ID列表
     */
    List<Integer> getFanList(Integer userId);

    /**
     * 获取好友列表（互相关注）
     * @param userId 用户ID
     * @return 好友用户ID列表
     */
    List<Integer> getFriendList(Integer userId);

    /**
     * 获取关注统计
     * @param userId 用户ID
     * @return 统计数据
     */
    FollowStatVO getFollowStat(Integer userId);
}
