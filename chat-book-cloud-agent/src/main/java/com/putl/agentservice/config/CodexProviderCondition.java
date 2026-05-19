package com.putl.agentservice.config;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class CodexProviderCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        String provider = context.getEnvironment().getProperty("agent.ai.provider", "anthropic");
        return "codex".equalsIgnoreCase(provider) || "openai".equalsIgnoreCase(provider);
    }
}
