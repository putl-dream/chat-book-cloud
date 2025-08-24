package fun.amireux.chat.book.langchain.exception;

public class AiException extends RuntimeException {
    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}