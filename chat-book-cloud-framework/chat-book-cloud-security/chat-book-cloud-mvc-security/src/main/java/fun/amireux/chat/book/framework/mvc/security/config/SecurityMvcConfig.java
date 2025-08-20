package fun.amireux.chat.book.framework.mvc.security.config;

import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAuthenticationEntryPoint;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true) // 默认关闭
public class SecurityMvcConfig {

    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;

    public SecurityMvcConfig(JsonAccessDeniedHandler jsonAccessDeniedHandler, JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint) {
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**") // 放行登录等公共接口
                        .permitAll()
                        .requestMatchers("/ws/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(auth -> auth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter()))
                )
                .exceptionHandling(ex -> {
                    ex.accessDeniedHandler(jsonAccessDeniedHandler);
                    ex.authenticationEntryPoint(jsonAuthenticationEntryPoint);
                })
                .build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        // 使用 NimbusJwtDecoder 解析
        SecretKey key = new SecretKeySpec(
                "PXrQbuCwXwOZzkML/Vm2S5rSwt1iybvmKtGDzVEu+Hc=".getBytes(),
                "HmacSHA256"
        );
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 根据 JWT 的 claims 设置角色
            Map<String, Object> claims = jwt.getClaims();
            String role = claims.get("role").toString(); // 或解析 JSON 得到角色
            if (role == null || role.isEmpty()) {
                log.warn("JWT claims does not contain role.");
                role = "ROLE_USER";
            }
            // Spring Security 的 hasRole() 会自动添加 ROLE_ 前缀，所以需要去掉前缀
            if (role.startsWith("ROLE_")) {
                role = role.substring(5); // 去掉 "ROLE_" 前缀
            }
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
        });
        return converter;
    }
}