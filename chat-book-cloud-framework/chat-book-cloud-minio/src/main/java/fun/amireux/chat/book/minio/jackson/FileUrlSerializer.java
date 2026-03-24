package fun.amireux.chat.book.minio.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fun.amireux.chat.book.minio.config.MinIOConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 将 MinIO 存储路径（如 images/xxx.png）序列化为可访问的完整 URL。
 */
@Component
public class FileUrlSerializer extends JsonSerializer<String> implements EnvironmentAware {

    private static String STATIC_PUBLIC_URL;
    private static String STATIC_ENDPOINT;
    private static String STATIC_BUCKET;

    private Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        this.environment = environment;
        // 从 Environment 直接绑定配置
        Binder binder = Binder.get(environment);
        MinIOConfigProperties props = binder.bind("minio", MinIOConfigProperties.class).orElse(null);
        if (props != null) {
            STATIC_PUBLIC_URL = props.getPublicUrl();
            STATIC_ENDPOINT = props.getEndpoint();
            STATIC_BUCKET = props.getBucket();
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

        // 使用静态配置
        String baseUrl = STATIC_PUBLIC_URL;
        if (baseUrl == null || baseUrl.isEmpty()) {
            baseUrl = STATIC_ENDPOINT;
        }

        if (baseUrl == null || baseUrl.isEmpty()) {
            gen.writeString(value);
            return;
        }

        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }

        if (STATIC_BUCKET != null && !STATIC_BUCKET.isEmpty()) {
            if (!STATIC_BUCKET.endsWith("/")) {
                baseUrl += STATIC_BUCKET + "/";
            } else {
                baseUrl += STATIC_BUCKET;
            }
        }

        gen.writeString(baseUrl + value);
    }
}
