package fun.amireux.chat.book.langchain;

import fun.amireux.chat.book.langchain.exception.AiException;
import fun.amireux.chat.book.langchain.factory.ChatModel;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class AiTemplate {
    // 默认模型（可配置）
    @Value("${ai.model:qwen-plus-2025-07-28}")
    private String defaultModel;

    private final Map<String, ChatModel> models;

    // 构造器注入所有实现 ChatModel 的 Bean
    private AiTemplate(Map<String, ChatModel> models) {
        this.models = models;
    }

    /**
     * 根据模型名称获取对应的模型服务
     */
    private ChatModel getModel(String modelName) {
        ChatModel model = models.get(modelName);
        if (model == null) {
            throw new IllegalArgumentException("Unsupported model: " + modelName);
        }
        return model;
    }


    /**
     * 简化版：自动选择模型，返回 CompletableFuture<String>
     */
    public CompletableFuture<String> chat(String prompt) {
        return chat(prompt, null);
    }

    public CompletableFuture<String> chat(String prompt, String modelName) {
        String model = modelName != null ? modelName : defaultModel;
        ChatModel chatModel = getModel(model);
        return chatModel.chat(prompt)
                .exceptionally(throwable -> {
                    // 统一异常处理（可扩展为事件、日志、告警）
                    throw new AiException("AI model [" + model + "] call failed", throwable);
                });
    }
}