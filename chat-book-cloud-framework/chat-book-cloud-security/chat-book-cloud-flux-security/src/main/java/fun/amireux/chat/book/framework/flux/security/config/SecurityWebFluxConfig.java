package fun.amireux.chat.book.framework.flux.security.config;

import fun.amireux.chat.book.framework.flux.security.core.JwtReactiveAuthenticationManager;
import fun.amireux.chat.book.framework.flux.security.core.JwtServerAuthenticationConverter;
import fun.amireux.chat.book.framework.flux.security.exresult.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.flux.security.exresult.JsonAuthenticationEntryPoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.ReactiveJwtAuthenticationConverter;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import reactor.core.publisher.Flux;

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
        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(new JwtReactiveAuthenticationManager());
        jwtFilter.setServerAuthenticationConverter(new JwtServerAuthenticationConverter());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
                        .pathMatchers("/pc-api/user/auth/**")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2ResourceServer(auth -> auth
                        .jwt(jwt -> jwt
                                .jwtDecoder(jwtDecoder())
                                .jwtAuthenticationConverter(jwtAuthenticationConverter())
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

    @Bean
    public ReactiveJwtAuthenticationConverter jwtAuthenticationConverter() {
        ReactiveJwtAuthenticationConverter converter = new ReactiveJwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            // 解析 claims
            String role = jwt.getClaimAsString("role");
            if (role == null) {
                role = "ROLE_USER";
            }
            return Flux.just(new SimpleGrantedAuthority(role));
        });
        return converter;
    }


}

