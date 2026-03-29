package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public abstract class AbstractOAuthLoginStrategy implements LoginStrategy {

    private final UserService userService;

    @Override
    public AuthenticatedUser authenticate(UserDTO user) {
        // OAuth 登录同样只产出认证结果，后续 Token 仍由统一服务签发。
        return userService.oauth2Login(user);
    }
}
