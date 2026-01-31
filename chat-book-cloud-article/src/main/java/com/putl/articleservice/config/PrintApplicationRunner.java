package com.putl.articleservice.config;

import cn.hutool.core.thread.ThreadUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PrintApplicationRunner implements ApplicationRunner {

    @Value("${server.port}")
    private String port;

    @Override
    public void run(ApplicationArguments args)  {
        ThreadUtil.execute(() -> {
            ThreadUtil.sleep(1, TimeUnit.SECONDS); // 延迟 1 秒，保证输出到结尾
            log.info(
                    "\n------------------------------------------------------------------\n" +
                    "\t\t 项目启动成功！请求地址：http://localhost:"+port +
                    "\n------------------------------------------------------------------\n");
        });
    }
}