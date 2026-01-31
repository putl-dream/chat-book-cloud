package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/auth/account")
@RequiredArgsConstructor
public class AccountLoginController {
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public CommonResult<LoginResult> login(LoginParam loginParam) {
        return CommonResult.success(new LoginResult(jwtUtil.generateToken(Map.of("username", loginParam.username()))));
    }

    // 注册
    @PostMapping("/registered")
    public CommonResult<Void> registered(RegisteredParam registeredParam) {
        return CommonResult.success();
    }



    public record LoginParam(String username, String password) {
    }
    public record RegisteredParam(String username, String password) {
    }
    public record LoginResult(String token) {
    }
}

