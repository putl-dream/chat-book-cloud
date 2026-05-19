package com.putl.agentservice.client.engine;

import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Getter
public class ArticleAiRequest {

    private final String model;

    private final Integer maxOutputTokens;

    private final Double temperature;

    private final String systemPrompt;

    private final List<Message> messages;

    private ArticleAiRequest(Builder builder) {
        this.model = builder.model;
        this.maxOutputTokens = builder.maxOutputTokens;
        this.temperature = builder.temperature;
        this.systemPrompt = builder.systemPrompt;
        this.messages = Collections.unmodifiableList(new ArrayList<>(builder.messages));
    }

    public static Builder builder() {
        return new Builder();
    }

    @Getter
    public static class Message {

        private final Role role;

        private final String content;

        private Message(Role role, String content) {
            this.role = role == null ? Role.USER : role;
            this.content = Objects.toString(content, "");
        }

        public static Message of(Role role, String content) {
            return new Message(role, content);
        }
    }

    public enum Role {
        USER,
        ASSISTANT
    }

    public static class Builder {

        private String model;

        private Integer maxOutputTokens;

        private Double temperature;

        private String systemPrompt;

        private final List<Message> messages = new ArrayList<>();

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder maxOutputTokens(Integer maxOutputTokens) {
            this.maxOutputTokens = maxOutputTokens;
            return this;
        }

        public Builder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public Builder systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        public Builder addMessage(Role role, String content) {
            this.messages.add(new Message(role, content));
            return this;
        }

        public Builder addMessage(Message message) {
            if (message != null) {
                this.messages.add(message);
            }
            return this;
        }

        public Builder addMessages(List<Message> messages) {
            if (messages != null) {
                messages.forEach(this::addMessage);
            }
            return this;
        }

        public Builder addUserMessage(String content) {
            return addMessage(Role.USER, content);
        }

        public Builder addAssistantMessage(String content) {
            return addMessage(Role.ASSISTANT, content);
        }

        public ArticleAiRequest build() {
            return new ArticleAiRequest(this);
        }
    }
}
