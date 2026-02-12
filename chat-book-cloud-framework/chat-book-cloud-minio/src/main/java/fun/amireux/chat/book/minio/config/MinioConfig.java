package fun.amireux.chat.book.minio.config;

import fun.amireux.chat.book.minio.utils.impl.MinioUpdateUtilImpl;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinIOConfigProperties.class)
@Import(MinioUpdateUtilImpl.class)
public class MinioConfig {

    private final MinIOConfigProperties minIOConfigProperties;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minIOConfigProperties.getEndpoint())
                .credentials(minIOConfigProperties.getAccessKey(), minIOConfigProperties.getSecretKey())
                .build();
    }
}
