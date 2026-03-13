package fun.amireux.chat.book.minio.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fun.amireux.chat.book.minio.config.MinIOConfigProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 将 MinIO 存储路径（如 images/xxx.png）序列化为可访问的完整 URL。
 *
 * <p>注意：@JsonSerialize(using=...) 场景下，序列化器实例常由 Jackson 自行创建，
 * 不会走 Spring 注入，导致配置为空。这里用静态缓存兜底：Spring 创建的组件实例会在启动时
 * 将配置写入 STATIC_CONFIG，Jackson 自建实例也能读取到配置，从而稳定拼接 URL。</p>
 */
@Component
@RequiredArgsConstructor
public class FileUrlSerializer extends JsonSerializer<String> {

    private static volatile MinIOConfigProperties STATIC_CONFIG;

    private final MinIOConfigProperties minIOConfigProperties;

    /**
     * 供 Jackson 在 @JsonSerialize(using=...) 场景下自行实例化使用。
     */
    public FileUrlSerializer() {
        this.minIOConfigProperties = null;
    }

    @PostConstruct
    public void initStaticConfig() {
        if (this.minIOConfigProperties != null) {
            STATIC_CONFIG = this.minIOConfigProperties;
        }
    }

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null || value.isEmpty()) {
            gen.writeNull();
            return;
        }

        if (value.startsWith("http://") || value.startsWith("https://")) {
            gen.writeString(value);
            return;
        }

        MinIOConfigProperties config = minIOConfigProperties != null ? minIOConfigProperties : STATIC_CONFIG;
        if (config == null) {
            gen.writeString(value);
            return;
        }

        String baseUrl = config.getPublicUrl();
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = config.getEndpoint();
        }

        if (baseUrl == null || baseUrl.isEmpty()) {
            gen.writeString(value);
            return;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        String bucket = config.getBucket();
        if (bucket != null && !bucket.isEmpty()) {
            if (!bucket.endsWith("/")) {
                bucket += "/";
            }
            baseUrl += bucket;
        }

        gen.writeString(baseUrl + value);
    }
}
