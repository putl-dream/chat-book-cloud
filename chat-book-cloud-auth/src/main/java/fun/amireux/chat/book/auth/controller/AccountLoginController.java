package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.controller.dto.CaptchaLoginRequest;
import fun.amireux.chat.book.auth.controller.dto.PasswordLoginRequest;
import fun.amireux.chat.book.auth.controller.dto.RefreshRequest;
import fun.amireux.chat.book.auth.controller.dto.RegisterRequest;
import fun.amireux.chat.book.auth.projectobject.LoginVO;
import fun.amireux.chat.book.auth.service.AuthApplicationService;
import fun.amireux.chat.book.auth.service.AuthTokenService;
import fun.amireux.chat.book.auth.service.CaptchaService;
import fun.amireux.chat.book.auth.service.command.CaptchaLoginCommand;
import fun.amireux.chat.book.auth.service.command.PasswordLoginCommand;
import fun.amireux.chat.book.auth.service.command.RegisterCommand;
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

    // 控制器只接收场景化请求，再转换成内部命令对象交给应用服务。
    @PostMapping("/login/password")
    public CommonResult<LoginVO> passwordLogin(@RequestBody PasswordLoginRequest request) {
        return CommonResult.success(authApplicationService.login(
                new PasswordLoginCommand(request.username(), request.email(), request.password())
        ));
    }

    @PostMapping("/login/captcha")
    public CommonResult<LoginVO> captchaLogin(@RequestBody CaptchaLoginRequest request) {
        return CommonResult.success(authApplicationService.login(
                new CaptchaLoginCommand(request.email(), request.captcha())
        ));
    }

    @PostMapping({"/register", "/registered"})
    public CommonResult<LoginVO> register(@RequestBody RegisterRequest request) {
        return CommonResult.success(authApplicationService.login(
                new RegisterCommand(request.email(), request.username(), request.password(), request.captcha())
        ));
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
