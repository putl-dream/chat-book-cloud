package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.command.LoginCommand;
import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2LoginStrategy implements LoginStrategy {

    private final UserService userService;

    @Override
    public boolean supports(LoginMethod loginMethod) {
        return LoginMethod.GOOGLE == loginMethod || LoginMethod.GITHUB == loginMethod;
    }

    @Override
    public AuthenticatedUser authenticate(LoginCommand command) {
        OAuthLoginCommand loginCommand = (OAuthLoginCommand) command;
        // Provider 差异已经由 resolver 层处理，这里只保留统一的 OAuth 登录逻辑。
        return userService.oauth2Login(loginCommand);
    }
}
