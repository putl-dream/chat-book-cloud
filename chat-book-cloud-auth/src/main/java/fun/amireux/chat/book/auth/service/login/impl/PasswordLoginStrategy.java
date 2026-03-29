package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import fun.amireux.chat.book.auth.utils.PwdUtil;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordLoginStrategy implements LoginStrategy {

    private final UserService userService;

    @Override
    public LoginMethod support() {
        return LoginMethod.PASSWORD;
    }

    @Override
    public AuthenticatedUser authenticate(UserDTO user) {
        if (StringUtils.isBlank(user.getPassword())) {
            throw new AuthenticationException("Password is required");
        }

        // 策略内部只处理“如何认证”，不参与 Token 签发。
        UserDO userDO = userService.getUserInfo(user);
        userService.ensureUserEnabled(userDO);
        if (!PwdUtil.checkPassword(user.getPassword(), userDO.getPassword())) {
            log.error("Password check failed");
            throw new AuthenticationException("Password is invalid");
        }

        UserInfoDO userInfo = userService.getUserProfile(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return new AuthenticatedUser(userDO.getId(), userInfo);
    }
}
