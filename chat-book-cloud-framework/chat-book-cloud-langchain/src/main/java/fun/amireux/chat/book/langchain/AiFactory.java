package fun.amireux.chat.book.langchain;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.image.ImageModel;
import fun.amireux.chat.book.langchain.model.AiProvider;
import fun.amireux.chat.book.langchain.model.qwen.ChatAiModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 图生文暂不支持
 * 原因 ： 通义没有实现 openAi 规范
 * 处理 ： 需要通过okhttp获取其他方式请求
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AiFactory {
    private final Map<String, ChatAiModel> chatAiModelMap;
    private final Map<String, EmbeddingModel> embeddingModelMap;
    private final Map<String, ImageModel> imageModelMap;

    @PostConstruct
    public void init() {
        log.info("引入模型Chat模型成功，工厂共有{}个", chatAiModelMap.size());
        log.info("引入模型Embedding模型成功，工厂共有{}个", embeddingModelMap.size());
        log.info("引入模型Image模型成功，工厂共有{}个", imageModelMap.size());
    }

    public ChatAiModel chat() {
        return getService(chatAiModelMap, AiProvider.QWEN, "Chat");
    }

    public ChatAiModel chat(AiProvider provider) {
        return getService(chatAiModelMap, provider, "Chat");
    }

    public EmbeddingModel embedding() {
        return getService(embeddingModelMap, AiProvider.QWEN, "Embedding");
    }

    public EmbeddingModel embedding(AiProvider provider) {
        return getService(embeddingModelMap, provider, "Embedding");
    }

    public ImageModel image() {
        return getService(imageModelMap, AiProvider.QWEN, "Image");
    }

    public ImageModel image(AiProvider provider) {
        return getService(imageModelMap, provider, "Image");
    }

    private <T> T getService(Map<String, T> serviceMap, AiProvider provider, String capability) {
        String key = provider.name().toLowerCase() + capability;
        T service = serviceMap.get(key);
        if (service == null) {
            throw new IllegalArgumentException(
                    "未找到 " + provider + " 的 " + capability + " 服务，请检查配置或依赖");
        }
        return service;
    }
}
