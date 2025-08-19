package fun.amireux.chat.book.gateway;

import fun.amireux.chat.book.framework.security.config.SecurityConfig;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Import;

@EnableDiscoveryClient
@Import(SecurityConfig.class)
@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {
        org.springframework.boot.SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
