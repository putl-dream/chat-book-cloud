package fun.amireux.chat.book.auth.service;

import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.service.command.LoginCommand;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import fun.amireux.chat.book.auth.service.login.LoginStrategyFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final LoginStrategyFactory loginStrategyFactory;
    private final AuthTokenService authTokenService;

    // 应用服务只负责编排：选策略、执行认证、签发 Token。
    public LoginVO login(LoginCommand command) {
        // 策略工厂负责根据登录方式选择策略。
        LoginStrategy strategy = loginStrategyFactory.getStrategy(command.loginMethod());
        // 策略负责执行认证。
        AuthenticatedUser authenticatedUser = strategy.authenticate(command);
        // 认证成功后，应用服务负责签发 Token。
        return authTokenService.issueTokens(authenticatedUser);
    }
}
