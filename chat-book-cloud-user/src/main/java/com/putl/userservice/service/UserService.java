package com.putl.userservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
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

    /**
     * 根据ids批量查询用户
     */
    List<UserVO> selectByIds(List<Integer> ids);

    //查询好友列表

    //分页查询用户
    IPage<UserVO> selectPage(Integer page, Integer size);

    /**
     * 更新用户信息
     * @param currentUserId 当前登录用户ID
     * @param userVO
     */
    void updateUser(Integer currentUserId, UserVO userVO);
}
