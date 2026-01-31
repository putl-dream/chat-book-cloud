package com.putl.userservice.controller;

import com.putl.userservice.component.CaptchaService;
import com.putl.userservice.util.Result;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "验证码服务")
@RestController
@RequestMapping("/user/captcha")
@RequiredArgsConstructor
public class CaptchaController {
    private final CaptchaService captchaService;

    @GetMapping
    public Result<String> captcha(@RequestParam("email") String email){
        String captcha = captchaService.sendCaptcha(email);
        return Result.success(captcha);
    }
}
