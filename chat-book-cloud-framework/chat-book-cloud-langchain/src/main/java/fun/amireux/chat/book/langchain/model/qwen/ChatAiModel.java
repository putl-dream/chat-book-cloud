package fun.amireux.chat.book.langchain.model.qwen;

import dev.langchain4j.service.MemoryId;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import reactor.core.publisher.Flux;


public interface ChatAiModel {
    String generate(String prompt);

    @SystemMessage("""
            你是一个乐于助人的AI助手，名叫小Q。请用简洁友好的语言回答用户。
            
            你可以使用以下工具来帮助用户：
            - 获取城市天气信息
            - 获取城市空气质量
            - 获取当前系统时间
            - 执行简单数学计算
            - 保存和读取临时数据
            
            当用户询问相关信息时，请主动使用对应的工具。
            """)
    String generate(@MemoryId String userId, @UserMessage String userMessage);

    @SystemMessage("""
            你是一个乐于助人的AI助手，名叫小Q。请用简洁友好的语言回答用户。
            
            你可以使用以下工具来帮助用户：
            - 获取城市天气信息
            - 获取城市空气质量
            - 获取当前系统时间
            - 执行简单数学计算
            - 保存和读取临时数据
            
            当用户询问相关信息时，请主动使用对应的工具。
            """)    Flux<String> generateStream(String prompt);

    Flux<String> generateStream(@MemoryId String userId, @UserMessage String userMessage);
}
