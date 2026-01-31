package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.enums.FriendRelationEnum;
import com.putl.userservice.mapper.UserFriendsMapper;
import com.putl.userservice.mapper.entity.UserFriendsDO;
import com.putl.userservice.service.UserFriendsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * [UserFriends]表服务接口
 *
 * @since 2025-01-15 16:49:35
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class UserFriendsServiceImpl extends ServiceImpl<UserFriendsMapper, UserFriendsDO> implements UserFriendsService {
    private final UserFriendsMapper userFriendsMapper;

    @Override
    public String addFriend(Integer userId, Integer friendId){
        if (userId == null || friendId == null) {
            return "参数错误";
        }
        // 如果本人已经关注对方，则关注返回已关注
        UserFriendsDO userFriendsDO = userFriendsMapper.selectOne(Wrappers.<UserFriendsDO>lambdaQuery().eq(UserFriendsDO::getUserId1, userId).eq(UserFriendsDO::getUserId2, friendId));
        if (userFriendsDO != null) return "已关注";

        //查询对方是否关注本人
        userFriendsDO = userFriendsMapper.selectOne(Wrappers.<UserFriendsDO>lambdaQuery().eq(UserFriendsDO::getUserId1, friendId).eq(UserFriendsDO::getUserId2, userId));
        // 如果对方没有关注本人，则关注对方
        if (userFriendsDO == null) {
            // 添加关注
            userFriendsDO = UserFriendsDO.builder().userId1(userId).userId2(friendId).status(0).build();
            userFriendsMapper.insert(userFriendsDO);
            return "关注成功";
        }
        userFriendsMapper.update(Wrappers.<UserFriendsDO>lambdaUpdate().set(UserFriendsDO::getStatus, 1).eq(UserFriendsDO::getUserId1, friendId).eq(UserFriendsDO::getUserId2, userId));
        return "恭喜成为好友";
    }

    // 查询本人与对方的关系
    @Override
    public FriendRelationEnum checkFriendRelation(Integer userId, Integer friendId){
        return null;
    }

    @Override
    public List<Integer> getFriendList(Integer userId){
        List<UserFriendsDO> dos1 = userFriendsMapper.selectList(Wrappers.<UserFriendsDO>lambdaQuery().eq(UserFriendsDO::getUserId1, userId).eq(UserFriendsDO::getStatus, 1));
        List<UserFriendsDO> dos2 = userFriendsMapper.selectList(Wrappers.<UserFriendsDO>lambdaQuery().eq(UserFriendsDO::getUserId2, userId).eq(UserFriendsDO::getStatus, 1));
        List<Integer> list1 = dos1.stream().map(UserFriendsDO::getUserId2).toList();
        List<Integer> list2 = dos2.stream().map(UserFriendsDO::getUserId1).toList();
        Set<Integer> set = new HashSet<>(list1);
        set.addAll(list2);
        return set.stream().toList();
    }
}

