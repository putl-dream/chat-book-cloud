package fun.amireux.chat.book.langchain.config;

import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.chat.StreamingChatLanguageModel;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.openai.OpenAiImageModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import dev.langchain4j.service.AiServices;
import fun.amireux.chat.book.langchain.model.qwen.ChatAiModel;
import fun.amireux.chat.book.langchain.tools.AiTool;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class QwenAiConfig {

    private final QwenAiProperties properties;

    // 基础模型
    @Bean
    @Qualifier("qwenai")
    public ChatLanguageModel qwenAiChatModel() {
        return OpenAiChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getChatModelName())
                .temperature(properties.getTemperature())
                .baseUrl(properties.getBaseUrl())
                .timeout(Duration.ofSeconds(60))
                .maxRetries(2)
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean
    @Qualifier("qwenai")
    public StreamingChatLanguageModel qwenAiStreamingChatModel() {
        return OpenAiStreamingChatModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getChatModelName())
                .temperature(properties.getTemperature())
                .baseUrl(properties.getBaseUrl())
                .timeout(Duration.ofSeconds(60))
                .logRequests(true)
                .logResponses(true)
                .build();
    }

    @Bean("qwenEmbedding")
    public EmbeddingModel qwenAiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(properties.getApiKey())
                .modelName(properties.getEmbeddingModelName())
                .baseUrl(properties.getBaseUrl())
                .dimensions(1024)
                .build();
    }

    @Bean("qwenImage")
    public ImageModel qwenAiImageModel() {
        return OpenAiImageModel.builder()
                .apiKey(properties.getApiKey())
                .baseUrl("https://dashscope.aliyuncs.com/api/v1/services/aigc/multimodal-generation/generation")
                .modelName(properties.getImageModelName())
                .size("1314*1092")
                .build();
    }

    /**
     * 聊天服务,具备 ：
     * - 工具支持
     * - 工具会话支持
     * - 记忆支持
     */
    @Bean("qwenChat")
    public ChatAiModel chatAiService(
            @Qualifier("qwenai") ChatLanguageModel chatModel,
            @Qualifier("qwenai") StreamingChatLanguageModel streamingChatModel,
            ApplicationContext applicationContext
    ) {
        Map<String, Object> toolBeans = applicationContext.getBeansWithAnnotation(AiTool.class);
        return AiServices.builder(ChatAiModel.class)
                .chatLanguageModel(chatModel)
                .streamingChatLanguageModel(streamingChatModel)
                .chatMemoryProvider(memoryId ->
                        MessageWindowChatMemory.builder()
                                .id(memoryId)
                                .maxMessages(20)  // 保留最近20条消息
                                .build()
                )
                .tools(toolBeans.values())
                .build();
    }
}