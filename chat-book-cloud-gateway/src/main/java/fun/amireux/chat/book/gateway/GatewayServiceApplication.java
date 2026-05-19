package fun.amireux.chat.book.gateway;

import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 网关服务应用启动类
 * <p>负责初始化并启动API网关微服务，提供路由转发、负载均衡、鉴权过滤等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication
public class GatewayServiceApplication {
    
    /**
     * 应用程序入口方法
     * <p>打印Nacos配置信息并启动网关服务</p>
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {

        System.out.println("NACOS_SERVER_ADDR=" + System.getenv("NACOS_SERVER_ADDR"));
        System.out.println("NACOS_USERNAME=" + System.getenv("NACOS_USERNAME"));
        System.out.println("NACOS_PASSWORD=" + System.getenv("NACOS_PASSWORD"));
        org.springframework.boot.SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
