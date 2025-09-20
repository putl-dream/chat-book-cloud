package fun.amireux.chat.book.gateway.config;

import com.alibaba.nacos.shaded.com.google.common.collect.Lists;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "auth")
public class AuthConfiguration {
    // ===================================== 白名单 配置 ==============================================
    private List<String> whiteList = Lists.newLinkedList();

    // ===================================== 鉴权 配置 ==============================================
    private String JWT_SECRET;
}
