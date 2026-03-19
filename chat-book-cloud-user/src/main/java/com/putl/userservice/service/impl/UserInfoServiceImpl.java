package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.putl.userservice.mapper.UserInfoMapper;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.UserInfoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserInfoServiceImpl implements UserInfoService {
    private final UserInfoMapper userInfoMapper;


    @Override
    @Transactional
    public void save(UserInfoDO info){
        userInfoMapper.insert(info);
    }

    @Override
    public UserInfoDO getByUserId(int id){
        return userInfoMapper.selectOne(Wrappers.<UserInfoDO>lambdaQuery().eq(UserInfoDO::getUserId,id));
    }

    @Override
    @Transactional
    public void update(UserInfoDO info) {
        UserInfoDO update = UserInfoDO.builder()
                .userId(info.getUserId())
                .username(info.getUsername())
                .photo(info.getPhoto())
                .profile(info.getProfile())
                .role(info.getRole())
                .build();
        userInfoMapper.update(update, Wrappers.<UserInfoDO>lambdaUpdate()
                .eq(UserInfoDO::getUserId, info.getUserId()));
    }
}
