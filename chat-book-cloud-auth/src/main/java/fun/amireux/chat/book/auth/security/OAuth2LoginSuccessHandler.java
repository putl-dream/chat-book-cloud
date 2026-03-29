package fun.amireux.chat.book.auth.security;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import fun.amireux.chat.book.auth.mapper.UserInfoMapper;
import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.projectobject.UserInfoDO;
import fun.amireux.chat.book.auth.service.AuthTokenService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final UserInfoMapper userInfoMapper;
    private final AuthTokenService authTokenService;

    @Value("${oauth2.success.redirect-url:http://localhost:5173/login}")
    private String redirectUrl;

    public OAuth2LoginSuccessHandler(
            UserService userService,
            UserInfoMapper userInfoMapper,
            AuthTokenService authTokenService
    ) {
        this.userService = userService;
        this.userInfoMapper = userInfoMapper;
        this.authTokenService = authTokenService;
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
        // OAuth2 登录成功后不再自行拼 claims，统一走 Token 服务签发与落库。
        LoginVO loginVO = authTokenService.issueTokens(userId, userInfo);

        // 前端现有回调协议仍保持不变。
        response.sendRedirect(redirectUrl
                + "?accessToken=" + loginVO.getAccessToken()
                + "&refreshToken=" + loginVO.getRefreshToken());
    }
}
