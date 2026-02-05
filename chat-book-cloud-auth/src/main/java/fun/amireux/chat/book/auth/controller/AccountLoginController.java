package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
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

    @PostMapping("/login")
    public CommonResult<String> login(@RequestBody UserDTO user) {
        return CommonResult.success(userService.login(user));
    }

    // 注册
    @PostMapping("/registered")
    public CommonResult<String> registered(@RequestBody UserDTO user) {
        user.setLoginMethod(LoginMethod.REGISTER);
        return CommonResult.success(userService.signIn(user));
    }

    @GetMapping("/captcha")
    public CommonResult<String> captcha(@RequestParam String email) {
        captchaService.sendCaptcha(email);
        return CommonResult.success("验证码已发送");
    }
}

