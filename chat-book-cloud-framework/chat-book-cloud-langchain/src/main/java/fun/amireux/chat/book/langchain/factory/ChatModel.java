package fun.amireux.chat.book.langchain.factory;

import java.util.concurrent.CompletableFuture;

// 统一的 AI 模型接口
public interface ChatModel {
    CompletableFuture<String> chat(String prompt);
}