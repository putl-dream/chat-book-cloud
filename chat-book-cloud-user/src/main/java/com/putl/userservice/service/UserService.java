package com.putl.userservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.entity.UserDO;

import java.util.List;

public interface UserService extends IService<UserDO> {
    /**
     * 根据id查询用户
     *
     * @param id
     * @return
     */
    UserVO selectById(int id);

    //查询好友列表
    List<UserChatVO> selectFriendList(int userId);

    //分页查询用户
    IPage<UserVO> selectPage(Integer page, Integer size);

    /**
     * 更新用户信息
     * @param userVO
     */
    void updateUser(UserVO userVO);
}