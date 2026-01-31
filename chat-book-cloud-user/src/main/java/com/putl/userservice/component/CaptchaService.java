package com.putl.userservice.component;

import com.putl.userservice.common.enums.EmailEnum;
import com.putl.userservice.util.RandomNumUtil;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class CaptchaService {
    private static final int CAPTCHA_LENGTH = 6;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EmailService emailService;

    public String sendCaptcha(@NotNull String email){
        String captcha = RandomNumUtil.getCaptchaByLen(CAPTCHA_LENGTH);
        // 将验证码保存到redis中，过期时间为60秒
        redisTemplate.opsForValue().set("captcha:" + email, captcha, 5 * 60L, TimeUnit.SECONDS);

        // 发送验证码到邮箱
        String captchaContent = EmailEnum.CAPTCHA.getDesc() + captcha + EmailEnum.THANKS.getDesc();
        emailService.sendTextMailMessage(email, EmailEnum.TITLE.getDesc(), captchaContent);

        return captcha;
    }

    public Boolean verifyCaptcha(String captcha, String email) {
        if (captcha == null) return false;
        boolean equals = captcha.equalsIgnoreCase(String.valueOf(redisTemplate.opsForValue().get("captcha:" + email)));
        if (equals) {
            redisTemplate.delete("captcha:" + email);
            return true;
        } else {
            return false;
        }
    }
}
