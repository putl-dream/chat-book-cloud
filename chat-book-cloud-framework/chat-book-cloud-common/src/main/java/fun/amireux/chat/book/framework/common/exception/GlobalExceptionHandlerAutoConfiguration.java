package fun.amireux.chat.book.framework.common.exception;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局异常处理器自动配置
 * 仅在 classpath 存在 spring-web (spring-boot-starter-web) 时生效
 */
@AutoConfiguration
@ConditionalOnClass(WebMvcConfigurer.class)
public class GlobalExceptionHandlerAutoConfiguration {

    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }
}
