package fun.amireux.chat.book.framework.flux.security.config;

import fun.amireux.chat.book.framework.flux.security.exresult.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.flux.security.exresult.JsonAuthenticationEntryPoint;
import fun.amireux.chat.book.framework.flux.security.filter.RequestBodyLoggingFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.web.server.SecurityWebFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityWebFluxConfig {

    private final JsonAccessDeniedHandler jsonAccessDeniedHandler;
    private final JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint;


    public SecurityWebFluxConfig(JsonAccessDeniedHandler jsonAccessDeniedHandler, JsonAuthenticationEntryPoint jsonAuthenticationEntryPoint) {
        this.jsonAccessDeniedHandler = jsonAccessDeniedHandler;
        this.jsonAuthenticationEntryPoint = jsonAuthenticationEntryPoint;
    }

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
                .addFilterBefore(new RequestBodyLoggingFilter(), SecurityWebFiltersOrder.FIRST)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/pc-api/user/auth/**")
                        .permitAll()
                        .pathMatchers("/pc-api/user/ws/**")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAfter(new RequestBodyLoggingFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
                .oauth2ResourceServer(auth -> auth
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                        ))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jsonAuthenticationEntryPoint)
                        .accessDeniedHandler(jsonAccessDeniedHandler))
                .build();
    }

    @Bean
    public ReactiveJwtDecoder jwtDecoder() {
        // 使用 NimbusJwtDecoder 解析 HS256
        SecretKey key = new SecretKeySpec(
                "PXrQbuCwXwOZzkML/Vm2S5rSwt1iybvmKtGDzVEu+Hc=".getBytes(),
                "HmacSHA256"
        );
        return NimbusReactiveJwtDecoder.withSecretKey(key).build();
    }
}

