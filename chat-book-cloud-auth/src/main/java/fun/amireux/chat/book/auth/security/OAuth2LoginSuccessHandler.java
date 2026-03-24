package fun.amireux.chat.book.auth.security;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
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
import java.util.Map;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @Value("${oauth2.success.redirect-url:http://localhost:5173/login}")
    private String redirectUrl;

    public OAuth2LoginSuccessHandler(UserService userService) {
        this.userService = userService;
        this.jwtUtil = new JwtUtil("chat-book", "auth-service");
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
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
            // GitHub may not return avatar in attributes, use a default avatar URL
            Object avatarUrl = attributes.get("avatar_url");
            if (avatarUrl != null) {
                avatar = avatarUrl.toString();
            }
        } else {
            // Unsupported provider
            response.sendRedirect(redirectUrl + "?error=unsupported_provider");
            return;
        }

        if (email == null || email.isEmpty()) {
            response.sendRedirect(redirectUrl + "?error=no_email");
            return;
        }

        // Find or create user
        UserDTO userDTO = new UserDTO();
        userDTO.setEmail(email);
        userDTO.setUsername(username);
        userDTO.setPhoto(avatar);
        userDTO.setLoginMethod(LoginMethod.valueOf(provider.toUpperCase()));

        Integer userId = userService.oauth2Login(userDTO);

        // Generate JWT token
        String token = jwtUtil.generateToken(Map.of("id", userId));

        // Redirect to frontend with token
        String redirectUrlWithToken = redirectUrl + "?token=" + token;
        response.sendRedirect(redirectUrlWithToken);
    }
}
