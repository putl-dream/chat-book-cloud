package fun.amireux.chat.book.auth.request;


import java.util.List;

public record AuthTokenRequest(String userId, String username, List<String> roles) {
}