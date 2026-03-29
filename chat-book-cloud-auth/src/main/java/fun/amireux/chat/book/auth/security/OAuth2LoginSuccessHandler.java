package fun.amireux.chat.book.auth.security;

import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.security.oauth.OAuthResolveException;
import fun.amireux.chat.book.auth.security.oauth.OAuthUserResolver;
import fun.amireux.chat.book.auth.security.oauth.OAuthUserResolverFactory;
import fun.amireux.chat.book.auth.service.command.OAuthLoginCommand;
import fun.amireux.chat.book.auth.service.AuthApplicationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final OAuthUserResolverFactory oAuthUserResolverFactory;
    private final AuthApplicationService authApplicationService;

    @Value("${oauth2.success.redirect-url:http://localhost:5173/login}")
    private String redirectUrl;

    public OAuth2LoginSuccessHandler(
            OAuthUserResolverFactory oAuthUserResolverFactory,
            AuthApplicationService authApplicationService
    ) {
        this.oAuthUserResolverFactory = oAuthUserResolverFactory;
        this.authApplicationService = authApplicationService;
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
        try {
            OAuthUserResolver resolver = oAuthUserResolverFactory.getResolver(provider);
            OAuthLoginCommand command = resolver.resolve(oauth2User.getAttributes());
            LoginVO loginVO = authApplicationService.login(command);

            // 前端现有回调协议仍保持不变。
            response.sendRedirect(redirectUrl
                    + "?accessToken=" + loginVO.getAccessToken()
                    + "&refreshToken=" + loginVO.getRefreshToken());
        } catch (OAuthResolveException ex) {
            response.sendRedirect(redirectUrl + "?error=" + ex.getErrorCode());
        }
    }
}
