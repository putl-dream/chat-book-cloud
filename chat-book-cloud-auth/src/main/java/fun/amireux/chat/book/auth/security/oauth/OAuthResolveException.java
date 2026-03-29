package fun.amireux.chat.book.auth.security.oauth;

public class OAuthResolveException extends RuntimeException {

    private final String errorCode;

    public OAuthResolveException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
