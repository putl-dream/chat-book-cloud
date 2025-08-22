package fun.amireux.chat.book.minio.config;

import io.minio.MinioClient;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "minio")
public class MinIOConfigProperties {
    private String endpoint;

    private String accessKey;

    private String secretKey;

    private String publicUrl;

    private String bucket;

    @PostConstruct
    public void init() {
        System.out.println(this.toString());
    }

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @Bean
    public String minioPublicUrl() {
        return publicUrl;
    }
}

