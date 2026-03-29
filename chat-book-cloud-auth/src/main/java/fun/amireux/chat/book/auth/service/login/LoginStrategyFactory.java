package fun.amireux.chat.book.auth.service.login;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class LoginStrategyFactory {

    private final Map<LoginMethod, LoginStrategy> strategyMap;

    public LoginStrategyFactory(List<LoginStrategy> strategies) {
        this.strategyMap = strategies.stream().collect(Collectors.toUnmodifiableMap(
                LoginStrategy::support,
                Function.identity()
        ));
    }

    // 按登录方式选择策略，替代原先 UserServiceImpl 中的 switch-case 分发。
    public LoginStrategy getStrategy(LoginMethod loginMethod) {
        LoginStrategy strategy = strategyMap.get(loginMethod);
        if (strategy == null) {
            throw new AuthenticationException("Unsupported login method: " + loginMethod);
        }
        return strategy;
    }
}
