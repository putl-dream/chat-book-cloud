package fun.amireux.chat.book.user.vo;

import lombok.Data;

@Data
public class LoginRequest {
    private String username;
    private String password;
}