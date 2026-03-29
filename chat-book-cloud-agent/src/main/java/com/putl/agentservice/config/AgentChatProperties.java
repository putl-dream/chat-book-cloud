package com.putl.agentservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "agent.chat")
public class AgentChatProperties {

    private Integer recentWindowSize = 12;

    private Integer recentWindowTtlMinutes = 30;

    private Integer notebookTtlMinutes = 120;

    private Long streamTimeoutMs = 60000L;

    private Integer streamExecutorCorePoolSize = 4;

    private Integer streamExecutorMaxPoolSize = 16;

    private Integer streamExecutorQueueCapacity = 200;
}
