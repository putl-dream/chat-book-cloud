package com.putl.articleservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PrintApplicationRunner {

    @Value("${server.port}")
    private String port;

    @EventListener(WebServerInitializedEvent.class)
    public void onWebServerReady(WebServerInitializedEvent event) {
        // 这时候 Web 服务器已经准备好了，端口也确定了
        log.info("\n------------------------------------------------------------------\n\t\t 项目启动成功！请求地址：http://localhost:{}\n------------------------------------------------------------------\n", port);
    }
}