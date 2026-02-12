package fun.amireux.chat.book.auth.config;

import fun.amireux.chat.book.auth.constant.MqConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MqConfig {

    @Bean
    public DirectExchange emailExchange() {
        return new DirectExchange(MqConstant.EMAIL_EXCHANGE);
    }

    @Bean
    public Queue emailCaptchaQueue() {
        return new Queue(MqConstant.EMAIL_CAPTCHA_QUEUE);
    }

    @Bean
    public Binding bindingEmailCaptcha(DirectExchange emailExchange, Queue emailCaptchaQueue) {
        return BindingBuilder.bind(emailCaptchaQueue).to(emailExchange).with(MqConstant.EMAIL_CAPTCHA_ROUTING_KEY);
    }
}
