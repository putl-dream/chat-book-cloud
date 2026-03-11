package com.putl.articleservice.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import fun.amireux.chat.book.minio.config.MinIOConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class FileUrlSerializer extends JsonSerializer<String> {

    private final MinIOConfigProperties minIOConfigProperties;

    /**
     * Jackson 反序列化时需要无参构造函数。
     * 此时 minIOConfigProperties 为 null，serialize 方法中已有 null 检查，
     * 会直接返回原始值（不拼接前缀），这对于反序列化场景是正确的行为。
     */
    public FileUrlSerializer() {
        this.minIOConfigProperties = null;
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

        MinIOConfigProperties config = minIOConfigProperties;
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

        String fullUrl = baseUrl + value;
        gen.writeString(fullUrl);
    }
}
