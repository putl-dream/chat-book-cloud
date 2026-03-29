package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.controller.dto.RefreshRequest;
import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.service.AuthApplicationService;
import fun.amireux.chat.book.auth.service.AuthTokenService;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/account")
@RequiredArgsConstructor
public class AccountLoginController {
    private final AuthApplicationService authApplicationService;
    private final CaptchaService captchaService;
    private final AuthTokenService authTokenService;

    // 登录入口改由应用服务编排，再委托策略和 Token 服务完成后续动作。

    @PostMapping("/login")
    public CommonResult<LoginVO> login(@RequestBody UserDTO user) {
        return CommonResult.success(authApplicationService.login(user));
    }

    @PostMapping("/registered")
    public CommonResult<LoginVO> registered(@RequestBody UserDTO user) {
        user.setLoginMethod(LoginMethod.REGISTER);
        return CommonResult.success(authApplicationService.login(user));
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
