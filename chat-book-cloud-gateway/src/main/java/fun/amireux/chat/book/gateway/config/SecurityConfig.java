package fun.amireux.chat.book.gateway.config;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Autowired
    private AuthConfiguration authConfiguration;
    private String[] excludePaths = new String[]{};

    @PostConstruct
    public void init() {
        excludePaths = authConfiguration.getWhiteList().toArray(new String[0]);
    }


    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        http
                .authorizeExchange(exchanges ->
                        exchanges
                                .pathMatchers("/").permitAll()              // 允许访问根路径
                                .pathMatchers("/favicon.ico").permitAll()   // 可选：放行图标
                                .pathMatchers(excludePaths).permitAll()
                                .anyExchange().authenticated()              // 其他路径需要认证
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> {
                        })
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable);

        return http.build();
    }
}