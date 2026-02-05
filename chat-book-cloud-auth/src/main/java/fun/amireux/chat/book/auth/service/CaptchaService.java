package fun.amireux.chat.book.auth.service;

import fun.amireux.chat.book.auth.component.EmailService;
import fun.amireux.chat.book.auth.utils.RandomNumUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptchaService {
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;

    private static final String CAPTCHA_PREFIX = "captcha:";
    private static final long CAPTCHA_EXPIRE_TIME = 5; // 5 minutes

    public void sendCaptcha(String email) {
        String code = RandomNumUtil.getCaptchaByLen(6);
        String key = CAPTCHA_PREFIX + email;
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_TIME, TimeUnit.MINUTES);
        
        emailService.sendTextMailMessage(email, "【ChatBook】验证码", "您的验证码是：" + code + "，有效期5分钟。");
        log.info("验证码发送成功: {} -> {}", email, code);
    }

    public boolean verifyCaptcha(String email, String code) {
        if (StringUtils.isAnyBlank(email, code)) {
            return false;
        }
        String key = CAPTCHA_PREFIX + email;
        String storedCode = redisTemplate.opsForValue().get(key);
        // Verify code
        if (code.equalsIgnoreCase(storedCode)) {
             // Delete after use
             redisTemplate.delete(key);
             return true;
        }
        return false;
    }
}
