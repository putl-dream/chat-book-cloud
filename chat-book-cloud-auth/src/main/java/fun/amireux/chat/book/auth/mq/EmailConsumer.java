package fun.amireux.chat.book.auth.mq;

import fun.amireux.chat.book.auth.component.EmailService;
import fun.amireux.chat.book.auth.constant.MqConstant;
import fun.amireux.chat.book.auth.model.dto.CaptchaEmailDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {

    private final EmailService emailService;

    @RabbitListener(queues = MqConstant.EMAIL_CAPTCHA_QUEUE)
    public void receiveCaptchaEmail(CaptchaEmailDTO captchaEmailDTO) {
        log.info("收到发送验证码邮件请求: {}", captchaEmailDTO);
        try {
            emailService.sendCaptchaMailMessage(
                captchaEmailDTO.getTo(),
                captchaEmailDTO.getCode(),
                captchaEmailDTO.getExpireTime()
            );
        } catch (Exception e) {
            log.error("发送验证码邮件失败", e);
            // 这里可以添加重试机制，或者抛出异常由RabbitMQ进行重试
        }
    }
}
