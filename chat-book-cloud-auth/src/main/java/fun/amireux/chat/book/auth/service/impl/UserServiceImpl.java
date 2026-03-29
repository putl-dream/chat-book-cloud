package fun.amireux.chat.book.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.mapper.UserMapper;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;
import fun.amireux.chat.book.auth.service.command.RegisterCommand;
import fun.amireux.chat.book.auth.service.login.AuthenticatedUser;
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
    private final CaptchaService captchaService;

    @Override
    public UserDO getUserByUsernameOrEmail(String username, String email) {
        UserDO userDO = null;
        if (StringUtils.isNotBlank(username)) {
            UserInfoDO userInfo = userInfoMapper.selectOne(
                    Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUsername, username)
            );
            if (userInfo != null) {
                userDO = userMapper.selectById(userInfo.getUserId());
            }
        }

        if (userDO == null && StringUtils.isNotBlank(email)) {
            userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, email));
        }

        if (userDO == null) {
            log.error("User not found");
            throw new AuthenticationException("User does not exist");
        }
        return userDO;
    }

    @Override
    @Transactional
    public AuthenticatedUser register(RegisterCommand command) {
        if (!captchaService.verifyCaptcha(command.email(), command.captcha())) {
            throw new AuthenticationException("Verification code is invalid");
        }

        if (userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, command.email())) != null) {
            log.error("Email already exists");
            throw new AuthenticationException("Email already exists");
        }

        if (userInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUsername, command.username())
        ) != null) {
            log.error("Username already exists");
            throw new AuthenticationException("Username already exists");
        }

        UserDO userDO = UserDO.builder()
                .email(command.email())
                .password(PwdUtil.hashPassword(command.password()))
                .status(0)
                .build();
        userMapper.insert(userDO);

        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(userDO.getId())
                .username(command.username())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        return new AuthenticatedUser(userDO.getId(), userInfo);
    }

    @Override
    @Transactional
    public AuthenticatedUser oauth2Login(OAuthLoginCommand user) {
        UserDO existingUser = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.email()));

        if (existingUser != null) {
            ensureUserEnabled(existingUser);
            UserInfoDO userInfo = getUserProfile(existingUser.getId());
            if (userInfo != null) {
                boolean updated = false;
                if (user.username() != null && !user.username().equals(userInfo.getUsername())) {
                    userInfo.setUsername(user.username());
                    updated = true;
                }
                if (user.photo() != null && !user.photo().equals(userInfo.getPhoto())) {
                    userInfo.setPhoto(user.photo());
                    updated = true;
                }
                if (updated) {
                    userInfoMapper.updateById(userInfo);
                }
            }
            return new AuthenticatedUser(existingUser.getId(), userInfo);
        }

        UserDO newUser = UserDO.builder()
                .email(user.email())
                .password(PwdUtil.hashPassword(""))
                .status(0)
                .build();
        userMapper.insert(newUser);

        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(newUser.getId())
                .username(user.username() != null ? user.username() : "user_" + newUser.getId())
                .photo(user.photo())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        log.info("OAuth user registered: {}, provider: {}", user.email(), user.loginMethod());
        return new AuthenticatedUser(newUser.getId(), userInfo);
    }

    @Override
    public UserInfoDO getUserProfile(Integer userId) {
        return userInfoMapper.selectOne(Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUserId, userId));
    }

    @Override
    public void ensureUserEnabled(UserDO userDO) {
        if (userDO != null && Integer.valueOf(USER_STATUS_DISABLED).equals(userDO.getStatus())) {
            throw new AuthenticationException("Account has been disabled");
        }
    }
}
