package fun.amireux.chat.book.auth.service.login;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.command.LoginCommand;

/**
 * 登录策略
 */
public interface LoginStrategy {
    // 是否支持当前登录方式
    boolean supports(LoginMethod loginMethod);

    // 登录
    AuthenticatedUser authenticate(LoginCommand command);
}
