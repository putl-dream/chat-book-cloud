package fun.amireux.chat.book.user.controller;

import fun.amireux.chat.book.framework.common.pojo.LoginRequest;
import fun.amireux.chat.book.framework.common.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/login")
    public String login() {
        return JwtUtil.generateToken(
                new LoginRequest("zhangsan", "111", "ADMIN")
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/logout")
    public String logout() {
        redisTemplate.opsForValue().set("hello", "redis");
        Object o = redisTemplate.opsForValue().get("hello");
        return "logout" +o;
    }
}