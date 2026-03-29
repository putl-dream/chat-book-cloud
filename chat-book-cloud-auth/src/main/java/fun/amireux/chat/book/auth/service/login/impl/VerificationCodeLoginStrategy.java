package fun.amireux.chat.book.auth.service.login.impl;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
import fun.amireux.chat.book.auth.service.login.LoginStrategy;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationCodeLoginStrategy implements LoginStrategy {

    private final UserService userService;
    private final CaptchaService captchaService;

    @Override
    public LoginMethod support() {
        return LoginMethod.VERIFICATION_CODE;
    }

    @Override
    public AuthenticatedUser authenticate(UserDTO user) {
        if (StringUtils.isBlank(user.getVerificationCode())) {
            throw new AuthenticationException("Verification code is required");
        }

        if (StringUtils.isBlank(user.getEmail())) {
            throw new AuthenticationException("Email is required");
        }

        if (!captchaService.verifyCaptcha(user.getEmail(), user.getVerificationCode())) {
            throw new AuthenticationException("Verification code is invalid or expired");
        }

        // 验证码校验通过后，后续用户装载逻辑与其他策略保持一致。
        UserDO userDO = userService.getUserInfo(user);
        userService.ensureUserEnabled(userDO);
        UserInfoDO userInfo = userService.getUserProfile(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return new AuthenticatedUser(userDO.getId(), userInfo);
    }
}
