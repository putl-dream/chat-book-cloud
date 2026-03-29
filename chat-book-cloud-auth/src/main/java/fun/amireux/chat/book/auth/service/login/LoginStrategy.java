package fun.amireux.chat.book.auth.service.login;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.dto.UserDTO;

/**
 * 登录策略
 */
public interface LoginStrategy {
    // 登录方式
    LoginMethod support();

    // 登录
    AuthenticatedUser authenticate(UserDTO user);
}
