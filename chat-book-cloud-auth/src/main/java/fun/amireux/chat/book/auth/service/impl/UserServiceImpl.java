package fun.amireux.chat.book.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import fun.amireux.chat.book.auth.mapper.UserMapper;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
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
    private final JwtUtil jwtUtil;

    @Override
    public String login(UserDTO user) {
        if (StringUtils.isAllBlank(user.getUsername(), user.getEmail())) {
            log.error("用户名或邮箱不能为空");
            throw new AuthenticationException("登录信息不完整");
        }

        if (StringUtils.isAllBlank(user.getPassword(), user.getVerificationCode())) {
            log.error("密码或验证码不能为空");
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
        UserDO userDO = getUserInfo(user);
        if (!userDO.getPassword().equals(user.getPassword())) {
            log.error("密码错误");
            throw new AuthenticationException("密码错误");
        }
        log.info("用户登录成功: {}", userDO.getEmail());
        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "email", userDO.getEmail()));
    }


    private String passwordLogin(UserDTO user) {
        UserDO userDO = getUserInfo(user);
        if (!userDO.getPassword().equals(user.getPassword())) {
            log.error("密码错误");
            throw new AuthenticationException("密码错误");
        }
        log.info("用户登录成功: {}", userDO.getUsername());
        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "username", userDO.getUsername()));
    }

    @Override
    public UserDO getUserInfo(UserDTO user) {
        UserDO userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getUsername, user.getUsername()));
        if (userDO == null)
            userDO = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.getEmail()));

        if (userDO == null) {
            log.error("用户不存在");
            throw new AuthenticationException("用户不存在");
        }
        return userDO;
    }

    @Override
    public String signIn(UserDTO signInVO) {
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
                .password(signInVO.getPassword())
                .build();

        userMapper.insert(userDO);
        return jwtUtil.generateToken(Map.of("id", userDO.getId(), "username", userDO.getUsername()));
    }
}
