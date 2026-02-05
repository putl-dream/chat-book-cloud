package com.putl.articleservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 映射图片
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:upload/images/");
        
        // 映射视频
        registry.addResourceHandler("/videos/**")
                .addResourceLocations("file:upload/videos/");
                
        // 映射其他
        registry.addResourceHandler("/others/**")
                .addResourceLocations("file:upload/others/");
    }
}
