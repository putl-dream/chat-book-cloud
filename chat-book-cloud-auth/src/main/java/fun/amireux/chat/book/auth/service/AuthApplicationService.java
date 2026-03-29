package fun.amireux.chat.book.auth.service;

import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import fun.amireux.chat.book.auth.service.login.LoginStrategyFactory;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthApplicationService {

    private final LoginStrategyFactory loginStrategyFactory;
    private final AuthTokenService authTokenService;

    // 应用服务只负责编排：选策略、执行认证、签发 Token。
    public LoginVO login(UserDTO user) {
        if (user.getLoginMethod() == null) {
            throw new AuthenticationException("Login method is required");
        }

        LoginStrategy strategy = loginStrategyFactory.getStrategy(user.getLoginMethod());
        AuthenticatedUser authenticatedUser = strategy.authenticate(user);
        return authTokenService.issueTokens(authenticatedUser);
    }
}
