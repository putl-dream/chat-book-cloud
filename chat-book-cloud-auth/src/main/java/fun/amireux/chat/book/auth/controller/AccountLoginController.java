package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import fun.amireux.chat.book.auth.service.UserService;
import fun.amireux.chat.book.auth.service.dto.UserDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/account")
@RequiredArgsConstructor
public class AccountLoginController {
    private final UserService userService;

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
}

