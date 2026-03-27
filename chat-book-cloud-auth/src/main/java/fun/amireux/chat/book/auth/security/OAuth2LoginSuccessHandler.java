package fun.amireux.chat.book.auth.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.RefreshTokenInfo;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.RefreshTokenService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final int ADMIN_ROLE_CODE = 1;

    private final UserService userService;
    private final UserInfoMapper userInfoMapper;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;

    @Value("${oauth2.success.redirect-url:http://localhost:5173/login}")
    private String redirectUrl;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            UserInfoMapper userInfoMapper,
            JwtUtil jwtUtil,
            RefreshTokenService refreshTokenService
    ) {
        this.userService = userService;
        this.userInfoMapper = userInfoMapper;
        this.jwtUtil = jwtUtil;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oauth2User = oauthToken.getPrincipal();
        String provider = oauthToken.getAuthorizedClientRegistrationId();

        Map<String, Object> attributes = oauth2User.getAttributes();
        String email;
        String username;
        String avatar = null;

        if ("google".equals(provider)) {
            email = (String) attributes.get("email");
            username = (String) attributes.get("name");
            avatar = (String) attributes.get("picture");
        } else if ("github".equals(provider)) {
            email = (String) attributes.get("email");
            username = (String) attributes.get("login");
            Object avatarUrl = attributes.get("avatar_url");
            if (avatarUrl != null) {
                avatar = avatarUrl.toString();
            }
        } else {
            response.sendRedirect(redirectUrl + "?error=unsupported_provider");
            return;
        }

        if (email == null || email.isEmpty()) {
            response.sendRedirect(redirectUrl + "?error=no_email");
            return;
        }

        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setUsername(username);
        userDTO.setPhoto(avatar);
        userDTO.setLoginMethod(LoginMethod.valueOf(provider.toUpperCase()));

        Integer userId = userService.oauth2Login(userDTO);
        UserInfoDO userInfo = userInfoMapper.selectOne(
                Wrappers.lambdaQuery(UserInfoDO.class).eq(UserInfoDO::getUserId, userId)
        );

        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("id", userId);
        if (userInfo != null && userInfo.getUsername() != null && !userInfo.getUsername().isBlank()) {
            claims.put("username", userInfo.getUsername());
        }
        claims.put("roles", resolveRoleClaim(userInfo));

        String accessToken  = jwtUtil.generateAccessToken(claims);
        String refreshToken = jwtUtil.generateRefreshToken(claims);

        // Store refresh token in Redis
        DecodedJWT refreshJwt = jwtUtil.verifyToken(refreshToken);
        String jti = jwtUtil.getJti(refreshJwt);
        long refreshExpSeconds = refreshJwt.getExpiresAt().getTime() / 1000;
        RefreshTokenInfo info = new RefreshTokenInfo(
                userId, Instant.now(), Instant.ofEpochSecond(refreshExpSeconds), null
        );
        refreshTokenService.store(jti, info, refreshExpSeconds);

        // Redirect with both tokens as query params
        response.sendRedirect(redirectUrl
                + "?accessToken="  + accessToken
                + "&refreshToken=" + refreshToken);
    }

    private String resolveRoleClaim(UserInfoDO userInfo) {
        if (userInfo != null && Integer.valueOf(ADMIN_ROLE_CODE).equals(userInfo.getRole())) {
            return "ROLE_ADMIN";
        }
        return "ROLE_USER";
    }
}
