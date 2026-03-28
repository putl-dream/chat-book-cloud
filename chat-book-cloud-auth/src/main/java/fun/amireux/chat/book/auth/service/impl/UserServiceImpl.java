package fun.amireux.chat.book.auth.service.impl;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.auth0.jwt.interfaces.DecodedJWT;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.mapper.UserMapper;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.projectobject.RefreshTokenInfo;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.RefreshTokenService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.auth.utils.PwdUtil;
import fun.amireux.chat.book.framework.common.exceptions.AuthenticationException;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    private static final int ADMIN_ROLE_CODE = 1;

    private final UserMapper userMapper;
    private final UserInfoMapper userInfoMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
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
        UserInfoDO userInfo = findUserInfo(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return buildToken(userDO, userInfo);
    }

    private LoginVO passwordLogin(UserDTO user) {
        if (StringUtils.isBlank(user.getPassword())) {
            throw new AuthenticationException("Password is required");
        }

        UserDO userDO = getUserInfo(user);
        if (!PwdUtil.checkPassword(user.getPassword(), userDO.getPassword())) {
            log.error("Password check failed");
            throw new AuthenticationException("Password is invalid");
        }

        UserInfoDO userInfo = findUserInfo(userDO.getId());
        log.info("User login succeeded: {}", userInfo != null ? userInfo.getUsername() : userDO.getEmail());
        return buildToken(userDO, userInfo);
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
                .build();
        userMapper.insert(userDO);

        UserInfoDO userInfo = UserInfoDO.builder()
                .userId(userDO.getId())
                .username(signInVO.getUsername())
                .role(0)
                .build();
        userInfoMapper.insert(userInfo);

        return buildToken(userDO, userInfo);
    }

    @Override
    @Transactional
    public Integer oauth2Login(UserDTO user) {
        UserDO existingUser = userMapper.selectOne(Wrappers.lambdaQuery(UserDO.class).eq(UserDO::getEmail, user.getEmail()));

        if (existingUser != null) {
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

    private LoginVO buildToken(UserDO userDO, UserInfoDO userInfo) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", userDO.getId());

        if (userInfo != null && StringUtils.isNotBlank(userInfo.getUsername())) {
            claims.put("username", userInfo.getUsername());
        }

        claims.put("roles", resolveRoleClaim(userInfo));

        String accessToken  = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        // Decode refresh token to extract jti and expiry
        DecodedJWT refreshJwt = jwtUtil.verifyToken(refreshToken);
        String jti = jwtUtil.getJti(refreshJwt);
        Instant refreshExpiresAt = refreshJwt.getExpiresAt().toInstant();

        // Store refresh token metadata in Redis
        RefreshTokenInfo info = new RefreshTokenInfo(
                userDO.getId(),
                Instant.now(),
                refreshExpiresAt,
                null
        );
        refreshTokenService.store(jti, info, refreshExpiresAt);

        return new LoginVO(accessToken, refreshToken, jwtUtil.getAccessExpirationSeconds());
    }

    private String resolveRoleClaim(UserInfoDO userInfo) {
        if (userInfo != null && Integer.valueOf(ADMIN_ROLE_CODE).equals(userInfo.getRole())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }
}
