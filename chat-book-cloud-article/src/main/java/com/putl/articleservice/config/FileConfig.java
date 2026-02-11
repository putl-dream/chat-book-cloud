package com.putl.articleservice.config;

import com.putl.articleservice.utils.ImageUtil;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;



@Configuration
public class FileConfig {

    @Value("${file.storage.base-url}")
    private String baseUrl;

    @PostConstruct
    public void init() {
        ImageUtil.setBaseUrl(baseUrl);
    }
}
