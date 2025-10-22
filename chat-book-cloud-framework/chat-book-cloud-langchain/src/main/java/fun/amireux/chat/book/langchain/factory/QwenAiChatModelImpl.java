package fun.amireux.chat.book.langchain.factory;

import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiTokenizer;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;

@Slf4j
@Component("qwen-plus-2025-07-28")
public class QwenAiChatModelImpl implements ChatModel {

    private final OpenAiChatModel model;
    private final OpenAiTokenizer tokenizer;


    @Resource
    @Qualifier("aiTaskExecutor")
    private ExecutorService executor;

    public QwenAiChatModelImpl(@Value("${ai.api-key}") String apiKey,
                               @Value("${ai.model}") String modelName,
                               @Value("${ai.base-url}") String baseUrl) {
        tokenizer = new OpenAiTokenizer("gpt-3.5-turbo");
        this.model = OpenAiChatModel.builder()
                .baseUrl(baseUrl)
                .apiKey(apiKey)
                .modelName(modelName)
                .tokenizer(tokenizer)
                .build();
    }

    @Override
    public CompletableFuture<String> chat(String prompt) {
        int inputTokenCount = tokenizer.estimateTokenCountInText(prompt);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String response = model.generate(prompt);
                int outputTokenCount = tokenizer.estimateTokenCountInText(response);
                // 总共消耗的token数
                log.info("本次消耗的token总数: {}", inputTokenCount + outputTokenCount);
                return response;
            } catch (Exception e) {
                // 注意：异常必须在这里抛出，否则不会被捕获
                throw new CompletionException("AI call failed for prompt: " + prompt, e);
            }
        }, executor);
    }
}