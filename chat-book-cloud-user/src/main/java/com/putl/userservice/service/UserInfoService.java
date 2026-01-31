package com.putl.userservice.service;

import com.putl.userservice.mapper.entity.UserInfoDO;

public interface UserInfoService {

    void save(UserInfoDO info);


    UserInfoDO getByUserId(int id);
}