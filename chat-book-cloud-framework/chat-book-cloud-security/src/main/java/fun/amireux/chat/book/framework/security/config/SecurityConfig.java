package fun.amireux.chat.book.framework.security.config;

import fun.amireux.chat.book.framework.security.converter.JwtServerAuthenticationConverter;
import fun.amireux.chat.book.framework.security.hander.JsonAccessDeniedHandler;
import fun.amireux.chat.book.framework.security.hander.JsonAuthenticationEntryPoint;
import fun.amireux.chat.book.framework.security.manager.JwtReactiveAuthenticationManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;

@Import({JsonAccessDeniedHandler.class, JsonAuthenticationEntryPoint.class})
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http, JsonAuthenticationEntryPoint entryPoint, JsonAccessDeniedHandler deniedHandler) {

        AuthenticationWebFilter jwtFilter = new AuthenticationWebFilter(new JwtReactiveAuthenticationManager());
        jwtFilter.setServerAuthenticationConverter(new JwtServerAuthenticationConverter());

        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(ex -> ex
//                        .pathMatchers("/**")
                        .pathMatchers("/user/auth/**")
                        .permitAll()
                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(entryPoint)
                        .accessDeniedHandler(deniedHandler))
                .build();
    }
}

