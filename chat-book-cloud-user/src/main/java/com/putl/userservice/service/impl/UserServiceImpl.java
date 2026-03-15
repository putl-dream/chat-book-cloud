package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.UserMapper;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.UserInfoService;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;
    private final UserInfoService userInfoService;

    @Override
    public UserVO selectById(int id) {
        UserInfoDO userInfo = userInfoService.getByUserId(id);
        log.debug("userInfo = {}", userInfo);
        UserDO user = this.getById(id);
        if (userInfo == null) {
            throw new RuntimeException("用户信息未找到(user_info缺失)，请联系管理员");
        }
        if (user == null) {
            throw new RuntimeException("用户账号信息未找到(user缺失)，请联系管理员");
        }
        String role = (userInfo.getRole() == RoleEnum.USER) ? "user" : "admin";
        return UserVO.builder().id(userInfo.getId()).userId(id).username(userInfo.getUsername()).email(user.getEmail()).photo(userInfo.getPhoto()).profile(userInfo.getProfile()).role(role).build();
    }

    /**
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<UserVO> selectPage(Integer page, Integer size) {
        Page<UserDO> user = userMapper.selectPage(new Page<>(page, size), Wrappers.<UserDO>lambdaQuery().orderByDesc(UserDO::getCreateTime));
        return user.convert(userDO -> selectById(userDO.getId()));
    }

    @Override
    @Transactional
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
