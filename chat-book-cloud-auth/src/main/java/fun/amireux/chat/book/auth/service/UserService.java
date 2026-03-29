package fun.amireux.chat.book.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;

public interface UserService extends IService<UserDO> {
    /**
     * 注册
     *
     * @return 已认证用户
     */
    AuthenticatedUser register(UserDTO signInVO);

    /**
     * 获取用户信息
     *
     * @param userDTO 用户名或邮箱
     * @return 用户信息
     */
    UserDO getUserInfo(UserDTO userDTO);

    /**
     * 获取用户资料信息
     *
     * @param userId 用户ID
     * @return 用户资料
     */
    UserInfoDO getUserProfile(Integer userId);

    /**
     * 确保用户可登录
     *
     * @param userDO 用户信息
     */
    void ensureUserEnabled(UserDO userDO);

    /**
     * OAuth2第三方登录
     *
     * @param user 用户信息
     * @return 已认证用户
     */
    AuthenticatedUser oauth2Login(UserDTO user);
}
