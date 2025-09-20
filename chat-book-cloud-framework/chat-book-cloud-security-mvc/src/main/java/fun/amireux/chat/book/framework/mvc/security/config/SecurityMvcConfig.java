package fun.amireux.chat.book.framework.mvc.security.config;

import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.mvc.security.exresult.JsonAuthenticationEntryPoint;
import fun.amireux.chat.book.framework.mvc.security.filter.RequestLoggingFilter;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Autowired
    private AuthConfiguration authConfiguration;
    private String[] excludePaths = new String[]{};

    @PostConstruct
    public void init() {
        excludePaths = authConfiguration.getAuth_whiteList().toArray(new String[0]);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(excludePaths) // 放行登录等公共接口
                        .permitAll()
                        .requestMatchers("/ws/**")
                        .permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(new RequestLoggingFilter(), BasicAuthenticationFilter.class)
                .oauth2ResourceServer(auth -> auth
                        .jwt(jwt -> jwt
                                .decoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        )
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
        SecretKey key = new SecretKeySpec(authConfiguration.getJWT_SECRET().getBytes(), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

        // 设置如何从 JWT 中提取 authorities（权限/角色）
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> claims = jwt.getClaims();
            List<String> roles = (List<String>) claims.get("role");

            if (roles == null || roles.isEmpty()) {
                log.warn("JWT claims does not contain role. Using default ROLE_USER.");
                roles = List.of("ROLE_USER");
            }

            // Spring Security 的 hasRole('ADMIN') 实际匹配的是 "ROLE_ADMIN"
            // 所以我们需要把 "ROLE_ADMIN" 转换为 "ADMIN"，让 Spring Security 自动补全为 "ROLE_ADMIN"
            // 或者直接返回 "ROLE_ADMIN"，但推荐去掉前缀由框架自动加
            return roles.stream()
                    .map(role -> {
                        // 去掉 ROLE_ 前缀，让 Spring Security 自动加上（更安全）
                        String authority = role.startsWith("ROLE_") ? role.substring(5) : role;
                        return new SimpleGrantedAuthority("ROLE_" + authority);
                    })
                    .collect(Collectors.toList());
        });
        return converter;
    }
}