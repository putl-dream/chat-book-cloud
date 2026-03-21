package fun.amireux.chat.book.auth.service;

import fun.amireux.chat.book.auth.constant.MqConstant;
import fun.amireux.chat.book.auth.model.dto.CaptchaEmailDTO;
import fun.amireux.chat.book.auth.utils.RandomNumUtil;
import fun.amireux.chat.book.framework.redis.constant.RedisKeyConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@Slf4j
@RequiredArgsConstructor
public class CaptchaService {
    private final StringRedisTemplate redisTemplate;
    private final RabbitTemplate rabbitTemplate;

    private static final long CAPTCHA_EXPIRE_TIME = 5; // 5 minutes

    @Value("${spring.profiles.active:dev}")
    private String env;

    public void sendCaptcha(String email) {
        String code = RandomNumUtil.getCaptchaByLen(6);
        String key = RedisKeyConstants.authCaptcha(env, email);
        redisTemplate.opsForValue().set(key, code, CAPTCHA_EXPIRE_TIME, TimeUnit.MINUTES);

        // 发送消息到 MQ
        CaptchaEmailDTO captchaEmailDTO = new CaptchaEmailDTO(email, code, (int) CAPTCHA_EXPIRE_TIME);
        rabbitTemplate.convertAndSend(MqConstant.EMAIL_EXCHANGE, MqConstant.EMAIL_CAPTCHA_ROUTING_KEY, captchaEmailDTO);

        log.info("验证码消息已发送到MQ: {} -> {}", email, code);
    }

    public boolean verifyCaptcha(String email, String code) {
        if (StringUtils.isAnyBlank(email, code)) {
            return false;
        }
        String key = RedisKeyConstants.authCaptcha(env, email);
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
