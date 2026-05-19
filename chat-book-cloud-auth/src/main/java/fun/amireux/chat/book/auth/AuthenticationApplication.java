package fun.amireux.chat.book.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 认证服务应用启动类
 * <p>负责初始化并启动认证微服务，提供用户身份验证、授权管理等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@SpringBootApplication
public class AuthenticationApplication {
    
    /**
     * 应用程序入口方法
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        SpringApplication.run(AuthenticationApplication.class, args);
    }
}
