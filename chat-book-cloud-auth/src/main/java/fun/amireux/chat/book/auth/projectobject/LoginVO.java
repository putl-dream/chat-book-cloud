package fun.amireux.chat.book.auth.projectobject;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginVO {
    @Schema(description = "JWT access token")
    private String accessToken;

    @Schema(description = "JWT refresh token (one-time use, rotated on each refresh)")
    private String refreshToken;

    @Schema(description = "Access token remaining lifetime in seconds")
    private long expiresIn;
}
