package fun.amireux.chat.book.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GatewayServiceApplication {
    public static void main(String[] args) {

        System.out.println("NACOS_SERVER_ADDR=" + System.getenv("NACOS_SERVER_ADDR"));
        System.out.println("NACOS_USERNAME=" + System.getenv("NACOS_USERNAME"));
        System.out.println("NACOS_PASSWORD=" + System.getenv("NACOS_PASSWORD"));
        org.springframework.boot.SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
