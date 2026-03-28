package com.putl.agentservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
public class AgentAsyncConfig {

    @Bean("agentChatStreamExecutor")
    public Executor agentChatStreamExecutor(AgentChatProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setThreadNamePrefix("agent-chat-stream-");
        executor.setCorePoolSize(Math.max(1, properties.getStreamExecutorCorePoolSize()));
        executor.setMaxPoolSize(Math.max(1, properties.getStreamExecutorMaxPoolSize()));
        executor.setQueueCapacity(Math.max(0, properties.getStreamExecutorQueueCapacity()));
        executor.initialize();
        return executor;
    }
}
