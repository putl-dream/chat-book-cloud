package com.putl.userservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.controller.vo.SignInVO;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.service.dto.LoginDTO;
import com.putl.userservice.util.Result;

import java.util.List;

public interface UserService extends IService<UserDO> {
    /**
     * 登录
     *
     * @param email
     * @param password
     * @return
     */
    LoginDTO login(String email, String password);

    /**
     * 根据邮箱查询用户
     *
     * @param email
     * @return
     */
    UserDO selectByEmail(String email);

    /**
     * 注册
     *
     * @return
     */
    Result<String> signIn(SignInVO signInVO);

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
}