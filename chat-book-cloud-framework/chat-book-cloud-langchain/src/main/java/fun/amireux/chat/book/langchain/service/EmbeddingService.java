package fun.amireux.chat.book.langchain.service;// 文件：EmbeddingService.java

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import dev.langchain4j.model.output.Response;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class EmbeddingService {

    private final String apiKey;
    private final String baseUrl;

    public EmbeddingService(@Value("${ai.api-key}") String apiKey,
                            @Value("${ai.base-url}") String baseUrl) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl;
    }

    public EmbeddingModel getEmbeddingModel(String modelName) {
        return OpenAiEmbeddingModel.builder()
                .apiKey(apiKey)
                .baseUrl(baseUrl)
                .dimensions(2048)
                .modelName(modelName)
                .build();
    }


    public List<float[]> embedTexts(String modelName, List<String> texts) {
        EmbeddingModel model = getEmbeddingModel(modelName);

        // 去重并转为 List 以保持顺序（使用 LinkedHashSet）
        List<String> uniqueTexts = new ArrayList<>(new LinkedHashSet<>(texts));
        // 创建映射来存储文本到嵌入向量的关联
        Map<String, float[]> embeddingMap = new HashMap<>();

        // 分批处理唯一文本，每批最多10个文本
        int batchSize = 10;
        for (int i = 0; i < uniqueTexts.size(); i += batchSize) {
            int endIndex = Math.min(i + batchSize, uniqueTexts.size());
            List<String> batch = uniqueTexts.subList(i, endIndex);

            // 创建文本段
            List<TextSegment> segments = batch.stream()
                    .map(TextSegment::from)
                    .collect(Collectors.toList());

            // 执行嵌入
            Response<List<Embedding>> response = model.embedAll(segments);

            // 收集结果并填充映射
            List<Embedding> embeddings = response.content();
            for (int j = 0; j < batch.size(); j++) {
                String text = batch.get(j);
                float[] vector = embeddings.get(j).vector();
                embeddingMap.put(text, vector);
            }
        }

        // 根据原始文本顺序构建结果列表
        List<float[]> result = new ArrayList<>();
        for (String text : texts) {
            // 从映射中获取嵌入向量（确保不为null，因为所有文本都处理过）
            result.add(embeddingMap.get(text));
        }

        return result;
    }
}