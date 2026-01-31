package com.putl.articleservice.config;


import com.putl.articleservice.ws.ArticleChannel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
public class WebSocketConfig {
    @Bean
    public ServerEndpointExporter serverEndpointExporter(){
        ServerEndpointExporter exporter = new ServerEndpointExporter();
        exporter.setAnnotatedEndpointClasses(ArticleChannel.class);
        return exporter;
    }
}
