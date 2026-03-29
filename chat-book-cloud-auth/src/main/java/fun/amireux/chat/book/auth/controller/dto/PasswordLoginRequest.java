package fun.amireux.chat.book.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordLoginRequest(
        @Schema(description = "用户名")
        String username,
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "密码")
        String password
) {
}
