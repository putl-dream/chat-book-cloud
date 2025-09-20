package fun.amireux.chat.book.auth.controller;

import fun.amireux.chat.book.auth.request.AuthTokenRequest;
import fun.amireux.chat.book.auth.utils.JwtUtil;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private JwtUtil jwtUtil;

    @GetMapping("/login")
    public CommonResult<String> login() {
        return CommonResult.success(jwtUtil.generateAccessToken(new AuthTokenRequest("111", "张三", List.of("ADMIN"))));
    }

}
