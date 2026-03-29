package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.controller.dto.RefreshRequest;
import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.service.AuthTokenService;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/account")
@RequiredArgsConstructor
public class AccountLoginController {
    private final UserService userService;
    private final CaptchaService captchaService;
    private final AuthTokenService authTokenService;

    // 登录与注册仍走用户领域服务，Token 生命周期统一委托给 AuthTokenService。

    @PostMapping("/login")
    public CommonResult<LoginVO> login(@RequestBody UserDTO user) {
        return CommonResult.success(userService.login(user));
    }

    @PostMapping("/registered")
    public CommonResult<LoginVO> registered(@RequestBody UserDTO user) {
        user.setLoginMethod(LoginMethod.REGISTER);
        return CommonResult.success(userService.signIn(user));
    }

    @GetMapping("/captcha")
    public CommonResult<String> captcha(@RequestParam String email) {
        captchaService.sendCaptcha(email);
        return CommonResult.success("验证码已发送");
    }

    @PostMapping("/refresh")
    public CommonResult<LoginVO> refresh(@RequestBody RefreshRequest request) {
        return CommonResult.success(authTokenService.refresh(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public CommonResult<Void> logout(@RequestBody RefreshRequest request) {
        authTokenService.revoke(request.getRefreshToken());
        return CommonResult.success();
    }
}
