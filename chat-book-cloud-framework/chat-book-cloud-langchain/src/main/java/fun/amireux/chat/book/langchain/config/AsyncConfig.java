package fun.amireux.chat.book.langchain.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// 配置类中定义线程池
@Configuration
public class AsyncConfig {

    @Bean("aiTaskExecutor")
    public ExecutorService aiExecutor() {
        return Executors.newFixedThreadPool(10, r -> {
            Thread t = new Thread(r);
            t.setName("ai-thread-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }
}