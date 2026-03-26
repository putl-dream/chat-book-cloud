package fun.amireux.chat.book.framework.mvc.security.config;

import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAuthenticationEntryPoint;
import fun.amireux.chat.book.framework.mvc.security.filter.UserContextFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@ComponentScan("fun.amireux.chat.book.framework.mvc.security")
public class SecurityMvcConfig {

    private final UserContextFilter userContextFilter;
    private final JsonAuthenticationEntryPoint authenticationEntryPoint;
    private final JsonAccessDeniedHandler accessDeniedHandler;

    public SecurityMvcConfig(
            UserContextFilter userContextFilter,
            JsonAuthenticationEntryPoint authenticationEntryPoint,
            JsonAccessDeniedHandler accessDeniedHandler
    ) {
        this.userContextFilter = userContextFilter;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                )

                .authorizeHttpRequests(auth -> auth
                        // 1. 静态资源或特殊路径放行
                        .requestMatchers("/ws/**", "/favicon.ico").permitAll()
                        // 2. 默认允许所有人访问，由业务逻辑或方法注解控制
                        .anyRequest().permitAll()
                )
                // 添加自定义身份过滤器
                .addFilterBefore(userContextFilter, BasicAuthenticationFilter.class)
                .build();
    }
}
