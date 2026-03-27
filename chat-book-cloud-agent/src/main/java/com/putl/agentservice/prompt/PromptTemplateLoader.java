package com.putl.agentservice.prompt;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class PromptTemplateLoader {

    public String load(String name) {
        ClassPathResource resource = new ClassPathResource("prompts/" + name);
        try {
            byte[] bytes = resource.getInputStream().readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load prompt template: " + name, ex);
        }
    }
}
