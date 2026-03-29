package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RegisterLoginStrategy implements LoginStrategy {

    private final UserService userService;

    @Override
    public LoginMethod support() {
        return LoginMethod.REGISTER;
    }

    @Override
    public AuthenticatedUser authenticate(UserDTO user) {
        // 注册成功后直接返回认证结果，由应用服务统一补发 Token。
        return userService.register(user);
    }
}
