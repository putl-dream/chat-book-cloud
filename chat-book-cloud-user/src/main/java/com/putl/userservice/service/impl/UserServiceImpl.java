package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import fun.amireux.chat.book.framework.common.context.UserContext;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.UserMapper;
import com.putl.userservice.mapper.entity.MessageDO;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.MessageService;
import com.putl.userservice.service.UserFriendsService;
import com.putl.userservice.service.UserInfoService;
import com.putl.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;
    private final UserInfoService userInfoService;
    private final UserFriendsService userFriendsService;
    private final MessageService messageService;

    @Override
    public UserVO selectById(int id){
        UserInfoDO userInfo = userInfoService.getByUserId(id);
        System.out.println("userInfo = " + userInfo);
        UserDO user = this.getById(id);
        if (userInfo == null) {
            throw new RuntimeException("用户信息未找到(user_info缺失)，请联系管理员");
        }
        String role = Objects.equals(userInfo.getRole(), RoleEnum.USER.getCode()) ? "user" : "admin";
        return UserVO.builder().id(userInfo.getId()).userId(id).username(userInfo.getUsername()).email(user.getEmail()).photo(userInfo.getPhoto()).profile(userInfo.getProfile()).role(role).build();
    }

    @Override
    public List<UserChatVO> selectFriendList(int userId){
        List<Integer> list = userFriendsService.getFriendList(userId);
        List<UserVO> userInfo = list.stream().map(this::selectById).toList();
        return userInfo.stream().map(userVO -> {
            MessageDO messageDO = messageService.queryLastMessage(userId, userVO.getUserId());
            System.err.println("messageDO = " + messageDO);
            if (messageDO == null) {
                messageDO = new MessageDO();
            }

            return UserChatVO.builder().id(userVO.getId()).userId(userVO.getUserId()).username(userVO.getUsername()).photo(userVO.getPhoto()).profile(userVO.getProfile()).lastChat(messageDO.getContent()).lastTime(messageDO.getSentTime()).build();
        }).toList();
    }

    /**
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<UserVO> selectPage(Integer page, Integer size){
        Page<UserDO> user = userMapper.selectPage(new Page<>(page, size), Wrappers.<UserDO>lambdaQuery().orderByDesc(UserDO::getCreateTime));
        return user.convert(userDO -> selectById(userDO.getId()));
    }

    @Override
    public void updateUser(UserVO userVO) {
        String userId = UserContext.getUserId();
        if (userId == null || !userId.equals(String.valueOf(userVO.getUserId()))) {
            throw new RuntimeException("无权修改他人信息");
        }
        
        UserInfoDO userInfo = UserInfoDO.builder()
                .id(userVO.getId())
                .userId(userVO.getUserId())
                .username(userVO.getUsername())
                .photo(userVO.getPhoto())
                .profile(userVO.getProfile())
                .build();
                
        userInfoService.update(userInfo);
    }
}
