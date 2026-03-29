package fun.amireux.chat.book.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.mapper.UserMapper;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.AuthTokenService;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.utils.PwdUtil;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private static final int USER_STATUS_DISABLED = 1;

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final AuthTokenService authTokenService;
    private final CaptchaService captchaService;

    @Override
    public LoginVO login(UserDTO user) {
        if (StringUtils.isAllBlank(user.getUsername(), user.getEmail())) {
            log.error("Username or email is required");
            throw new AuthenticationException("Login payload is incomplete");
        }

        if (user.getLoginMethod() == null) {
            log.error("Login method is required");
            throw new AuthenticationException("Login method is required");
        }

        return switch (user.getLoginMethod()) {
            case VERIFICATION_CODE -> verificationCodeLogin(user);
            case REGISTER -> signIn(user);
            default -> passwordLogin(user);
        };
    }

    private LoginVO verificationCodeLogin(UserDTO user) {
        if (StringUtils.isBlank(user.getVerificationCode())) {
            throw new AuthenticationException("Verification code is required");
        }

        if (StringUtils.isBlank(user.getEmail())) {
            throw new AuthenticationException("Email is required");
        }

        if (!captchaService.verifyCaptcha(user.getEmail(), user.getVerificationCode())) {
            throw new AuthenticationException("Verification code is invalid or expired");
        }

        UserDO userDO = getUserInfo(user);
        validateUserStatus(userDO);
        UserInfoDO userInfo = findUserInfo(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return authTokenService.issueTokens(userDO.getId(), userInfo);
    }

    private LoginVO passwordLogin(UserDTO user) {
        if (StringUtils.isBlank(user.getPassword())) {
            throw new AuthenticationException("Password is required");
        }

        UserDO userDO = getUserInfo(user);
        validateUserStatus(userDO);
        if (!PwdUtil.checkPassword(user.getPassword(), userDO.getPassword())) {
            log.error("Password check failed");
            throw new AuthenticationException("Password is invalid");
        }

        UserInfoDO userInfo = findUserInfo(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return authTokenService.issueTokens(userDO.getId(), userInfo);
    }

    @Override
    public UserDO getUserInfo(UserDTO user) {
        UserDO userDO = null;
        if (StringUtils.isNotBlank(user.getUsername())) {
            UserInfoDO userInfo = userInfoMapper.selectOne(
                    Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUsername, user.getUsername())
            );
            if (userInfo != null) {
                userDO = userMapper.selectById(userInfo.getUserId());
            }
        }

        if (userDO == null && StringUtils.isNotBlank(user.getEmail())) {
            userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.getEmail()));
        }

        if (userDO == null) {
            log.error("User not found");
            throw new AuthenticationException("User does not exist");
        }
        return userDO;
    }

    @Override
    @Transactional
    public LoginVO signIn(UserDTO signInVO) {
        if (!captchaService.verifyCaptcha(signInVO.getEmail(), signInVO.getVerificationCode())) {
            throw new AuthenticationException("Verification code is invalid");
        }

        if (userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, signInVO.getEmail())) != null) {
            log.error("Email already exists");
            throw new AuthenticationException("Email already exists");
        }

        if (userInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUsername, signInVO.getUsername())
        ) != null) {
            log.error("Username already exists");
            throw new AuthenticationException("Username already exists");
        }

        UserDO userDO = UserDO.builder()
                .email(signInVO.getEmail())
                .password(PwdUtil.hashPassword(signInVO.getPassword()))
                .status(0)
                .build();
        userMapper.insert(userDO);

        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(userDO.getId())
                .username(signInVO.getUsername())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        return authTokenService.issueTokens(userDO.getId(), userInfo);
    }

    @Override
    @Transactional
    public Integer oauth2Login(UserDTO user) {
        UserDO existingUser = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.getEmail()));

        if (existingUser != null) {
            validateUserStatus(existingUser);
            UserInfoDO userInfo = findUserInfo(existingUser.getId());
            if (userInfo != null) {
                boolean updated = false;
                if (user.getUsername() != null && !user.getUsername().equals(userInfo.getUsername())) {
                    userInfo.setUsername(user.getUsername());
                    updated = true;
                }
                if (user.getPhoto() != null && !user.getPhoto().equals(userInfo.getPhoto())) {
                    userInfo.setPhoto(user.getPhoto());
                    updated = true;
                }
                if (updated) {
                    userInfoMapper.updateById(userInfo);
                }
            }
            return existingUser.getId();
        }

        UserDO newUser = UserDO.builder()
                .email(user.getEmail())
                .password(PwdUtil.hashPassword(""))
                .status(0)
                .build();
        userMapper.insert(newUser);

        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(newUser.getId())
                .username(user.getUsername() != null ? user.getUsername() : "user_" + newUser.getId())
                .photo(user.getPhoto())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        log.info("OAuth user registered: {}, provider: {}", user.getEmail(), user.getLoginMethod());
        return newUser.getId();
    }

    private UserInfoDO findUserInfo(Integer userId) {
        return userInfoMapper.selectOne(Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUserId, userId));
    }

    private void validateUserStatus(UserDO userDO) {
        if (userDO != null && Integer.valueOf(USER_STATUS_DISABLED).equals(userDO.getStatus())) {
            throw new AuthenticationException("Account has been disabled");
        }
    }
}
