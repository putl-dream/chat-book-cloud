package fun.amireux.chat.book.auth.service.login;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LoginStrategyFactory {

    private final List<LoginStrategy> strategies;

    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        this.strategies = List.copyOf(strategies);
    }

    // 按登录方式选择策略，替代原先 UserServiceImpl 中的 switch-case 分发。
    public LoginStrategy getStrategy(LoginMethod loginMethod) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(loginMethod))
                .findFirst()
                .orElseThrow(() -> new AuthenticationException("Unsupported login method: " + loginMethod));
    }
}
