package fun.amireux.chat.book.framework.common.exceptions;

public class AuthenticationException extends RuntimeException{

    public AuthenticationException(String message) {
        super(message);
    }

    public AuthenticationException(String message, Throwable cause) {
        super(message, cause);
    }
}
