package fun.amireux.chat.book.langchain.model;

import lombok.Getter;

@Getter
public enum AiProvider {
    QWEN,
    AZURE,
    OLLAMA,
    CLAUDE,
    LOCAL
}