package fun.amireux.chat.book.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;
import fun.amireux.chat.book.auth.service.command.RegisterCommand;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;

public interface UserService extends IService<UserDO> {
    /**
     * 注册
     *
     * @return 已认证用户
     */
    AuthenticatedUser register(RegisterCommand command);

    /**
     * 获取用户信息
     *
     * @param username 用户名
     * @param email 邮箱
     * @return 用户信息
     */
    UserDO getUserByUsernameOrEmail(String username, String email);

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
    AuthenticatedUser oauth2Login(OAuthLoginCommand user);
}
