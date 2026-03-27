package fun.amireux.chat.book.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshRequest {
    @Schema(description = "The refresh token issued during login or the previous refresh")
    private String refreshToken;
}
