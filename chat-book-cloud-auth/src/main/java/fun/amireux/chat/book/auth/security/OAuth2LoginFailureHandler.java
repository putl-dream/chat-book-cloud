package fun.amireux.chat.book.auth.security;

import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2LoginFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpStatus.OK.value());
        CommonResult<Object> result = CommonResult.error(HttpStatus.UNAUTHORIZED.value(), "OAuth登录失败: " + exception.getMessage());
        response.getWriter().write(result.toString());
    }
}
