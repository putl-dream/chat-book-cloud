package fun.amireux.chat.book.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.mapper.UserMapper;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.utils.PwdUtil;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final JwtUtil jwtUtil;
    private final CaptchaService captchaService;

    @Override
    public String login(UserDTO user) {
        if (StringUtils.isAllBlank(user.getUsername(), user.getEmail())) {
            log.error("用户名或邮箱不能为空");
            throw new AuthenticationException("登录信息不完整");
        }

        if (user.getLoginMethod() == null) {
            log.error("登录方式不能为空");
            throw new AuthenticationException("登录方式不能为空");
        }

        return switch (user.getLoginMethod()) {
            case VERIFICATION_CODE -> verificationCodeLogin(user);
            case REGISTER -> signIn(user);
            default -> passwordLogin(user);
        };
    }

    private String verificationCodeLogin(UserDTO user) {
        if (StringUtils.isBlank(user.getVerificationCode())) {
            throw new AuthenticationException("验证码不能为空");
        }
        
        // Use email for verification code login usually
        if (StringUtils.isBlank(user.getEmail())) {
             throw new AuthenticationException("邮箱不能为空");
        }

        if (!captchaService.verifyCaptcha(user.getEmail(), user.getVerificationCode())) {
            throw new AuthenticationException("验证码错误或已过期");
        }

        UserDO userDO = getUserInfo(user);
        log.info("用户登录成功: {}", userDO.getEmail());
        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "email", userDO.getEmail()));
    }


    private String passwordLogin(UserDTO user) {
        if (StringUtils.isBlank(user.getPassword())) {
            throw new AuthenticationException("密码不能为空");
        }
        UserDO userDO = getUserInfo(user);
        if (!PwdUtil.checkPassword(user.getPassword(), userDO.getPassword())) {
            log.error("密码错误");
            throw new AuthenticationException("密码错误");
        }
        log.info("用户登录成功: {}", userDO.getUsername());
        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "username", userDO.getUsername()));
    }

    @Override
    public UserDO getUserInfo(UserDTO user) {
        UserDO userDO = null;
        if (StringUtils.isNotBlank(user.getUsername())) {
             userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, user.getUsername()));
        }
        if (userDO == null && StringUtils.isNotBlank(user.getEmail())) {
            userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.getEmail()));
        }

        if (userDO == null) {
            log.error("用户不存在");
            throw new AuthenticationException("用户不存在");
        }
        return userDO;
    }

    @Override
    public String signIn(UserDTO signInVO) {
        // Verify Captcha for registration
        if (!captchaService.verifyCaptcha(signInVO.getEmail(), signInVO.getVerificationCode())) {
             throw new AuthenticationException("验证码错误");
        }

        if (userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, signInVO.getEmail())) != null) {
            log.error("邮箱已存在");
            throw new AuthenticationException("邮箱已存在");
        }
        if (userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, signInVO.getUsername())) != null) {
            log.error("用户名已存在");
            throw new AuthenticationException("用户名已存在");
        }

        UserDO userDO = UserDO.builder()
                .username(signInVO.getUsername())
                .email(signInVO.getEmail())
                .password(PwdUtil.hashPassword(signInVO.getPassword()))
                .build();

        userMapper.insert(userDO);

        // 注册用户信息
        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(userDO.getId())
                .username(signInVO.getUsername())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "username", userDO.getUsername()));
    }
}
