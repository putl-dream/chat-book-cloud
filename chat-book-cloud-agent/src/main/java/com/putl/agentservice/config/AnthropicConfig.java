package com.putl.agentservice.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AnthropicProperties.class)
public class AnthropicConfig {
}
