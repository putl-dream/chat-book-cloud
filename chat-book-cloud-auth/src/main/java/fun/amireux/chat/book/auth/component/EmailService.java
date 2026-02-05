package fun.amireux.chat.book.auth.component;

import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private ResourceLoader resourceLoader;

    @Value("${spring.mail.username}")
    private String sendMailer;

    /**
     * 缓存的验证码邮件模板
     */
    private String captchaEmailTemplate;

    /**
     * 初始化时加载邮件模板
     */
    @PostConstruct
    public void init() {
        loadCaptchaEmailTemplate();
    }

    /**
     * 加载验证码邮件模板
     */
    private void loadCaptchaEmailTemplate() {
        try {
            Resource resource = resourceLoader.getResource("classpath:templates/email/captcha.html");
            captchaEmailTemplate = resource.getContentAsString(StandardCharsets.UTF_8);
            logger.info("验证码邮件模板加载成功");
        } catch (IOException e) {
            logger.error("加载验证码邮件模板失败：{}", e.getMessage(), e);
            // 使用备用的简单模板
            captchaEmailTemplate = """
                    <html>
                    <body style="font-family: sans-serif; padding: 20px;">
                        <h2>【ChatBook】验证码</h2>
                        <p>您的验证码是：<strong style="font-size: 24px; color: #667eea;">{{code}}</strong></p>
                        <p>有效期为 {{expireTime}} 分钟，请尽快使用。</p>
                    </body>
                    </html>
                    """;
        }
    }

    @Async
    public void sendTextMailMessage(String to, String title, String text) {
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true);
            mimeMessageHelper.setFrom(sendMailer);
            mimeMessageHelper.setTo(to.split(","));
            mimeMessageHelper.setSubject(title);
            mimeMessageHelper.setText(text);
            mimeMessageHelper.setSentDate(new Date());

            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            logger.info("发送邮件成功：{} -> {}", sendMailer, to);

        } catch (MessagingException e) {
            logger.error("发送邮件失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 发送验证码邮件（HTML模板）
     *
     * @param to         收件人邮箱
     * @param code       验证码
     * @param expireTime 有效期（分钟）
     */
    @Async
    public void sendCaptchaMailMessage(String to, String code, int expireTime) {
        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(javaMailSender.createMimeMessage(), true, "UTF-8");
            mimeMessageHelper.setFrom(sendMailer);
            mimeMessageHelper.setTo(to.split(","));
            mimeMessageHelper.setSubject("【ChatBook】邮箱验证码");
            mimeMessageHelper.setText(buildCaptchaEmailContent(code, expireTime), true);
            mimeMessageHelper.setSentDate(new Date());

            javaMailSender.send(mimeMessageHelper.getMimeMessage());
            logger.info("发送验证码邮件成功：{} -> {}", sendMailer, to);

        } catch (MessagingException e) {
            logger.error("发送验证码邮件失败：{}", e.getMessage(), e);
        }
    }

    /**
     * 构建验证码邮件内容（替换模板中的占位符）
     */
    private String buildCaptchaEmailContent(String code, int expireTime) {
        return captchaEmailTemplate
                .replace("{{code}}", code)
                .replace("{{expireTime}}", String.valueOf(expireTime));
    }
}

