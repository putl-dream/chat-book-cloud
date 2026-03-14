package com.putl.socialservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.socialservice.entity.UserFollowDO;
import com.putl.socialservice.enums.FriendRelationEnum;
import com.putl.socialservice.mapper.UserFollowMapper;
import com.putl.socialservice.service.UserFollowService;
import com.putl.socialservice.vo.FollowStatVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户关注服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserFollowServiceImpl extends ServiceImpl<UserFollowMapper, UserFollowDO> implements UserFollowService {

    @Override
    @Transactional
    public String follow(Integer userId, Integer followId) {
        if (userId == null || followId == null) {
            return "参数错误";
        }
        if (userId.equals(followId)) {
            return "不能关注自己";
        }

        // 检查是否已经关注
        UserFollowDO existing = baseMapper.selectOne(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId)
                        .eq(UserFollowDO::getFollowId, followId));

        if (existing != null) {
            return "已关注";
        }

        // 添加关注记录
        UserFollowDO userFollowDO = UserFollowDO.builder()
                .userId(userId)
                .followId(followId)
                .build();
        baseMapper.insert(userFollowDO);

        // 检查对方是否也关注了我（互相关注）
        UserFollowDO reverseFollow = baseMapper.selectOne(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, followId)
                        .eq(UserFollowDO::getFollowId, userId));

        if (reverseFollow != null) {
            return "恭喜成为好友";
        }
        return "关注成功";
    }

    @Override
    @Transactional
    public String unfollow(Integer userId, Integer followId) {
        if (userId == null || followId == null) {
            return "参数错误";
        }

        // 删除关注记录
        int deleted = baseMapper.delete(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId)
                        .eq(UserFollowDO::getFollowId, followId));

        if (deleted > 0) {
            return "取消关注成功";
        }
        return "未关注该用户";
    }

    @Override
    public FriendRelationEnum checkFriendRelation(Integer userId, Integer targetUserId) {
        if (userId == null || targetUserId == null) {
            return FriendRelationEnum.Not_Friend;
        }

        // 查询 userId → targetUserId 的关注记录
        UserFollowDO myFollow = baseMapper.selectOne(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId)
                        .eq(UserFollowDO::getFollowId, targetUserId));

        if (myFollow == null) {
            return FriendRelationEnum.Not_Friend;
        }

        // 查询 targetUserId → userId 的关注记录（互相关注）
        UserFollowDO reverseFollow = baseMapper.selectOne(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, targetUserId)
                        .eq(UserFollowDO::getFollowId, userId));

        if (reverseFollow != null) {
            return FriendRelationEnum.Friend;
        }
        return FriendRelationEnum.Follow;
    }

    @Override
    public List<Integer> getFollowList(Integer userId) {
        List<UserFollowDO> list = baseMapper.selectList(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId)
                        .orderByDesc(UserFollowDO::getCreateTime));
        return list.stream().map(UserFollowDO::getFollowId).toList();
    }

    @Override
    public List<Integer> getFanList(Integer userId) {
        List<UserFollowDO> list = baseMapper.selectList(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getFollowId, userId)
                        .orderByDesc(UserFollowDO::getCreateTime));
        return list.stream().map(UserFollowDO::getUserId).toList();
    }

    @Override
    public List<Integer> getFriendList(Integer userId) {
        // 获取我关注的用户
        List<UserFollowDO> myFollows = baseMapper.selectList(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId));
        Set<Integer> myFollowing = new HashSet<>();
        for (UserFollowDO follow : myFollows) {
            myFollowing.add(follow.getFollowId());
        }

        // 获取关注我的用户
        List<UserFollowDO> myFans = baseMapper.selectList(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getFollowId, userId));

        // 交集即为互相关注（好友）
        Set<Integer> friends = new HashSet<>();
        for (UserFollowDO fan : myFans) {
            if (myFollowing.contains(fan.getUserId())) {
                friends.add(fan.getUserId());
            }
        }
        return friends.stream().toList();
    }

    @Override
    public FollowStatVO getFollowStat(Integer userId) {
        // 关注数
        Long followCount = baseMapper.selectCount(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getUserId, userId));

        // 粉丝数
        Long fanCount = baseMapper.selectCount(
                Wrappers.<UserFollowDO>lambdaQuery()
                        .eq(UserFollowDO::getFollowId, userId));

        // 好友数（互相关注）
        List<Integer> friendList = getFriendList(userId);

        return FollowStatVO.builder()
                .followCount(followCount.intValue())
                .fanCount(fanCount.intValue())
                .friendCount(friendList.size())
                .build();
    }
}
