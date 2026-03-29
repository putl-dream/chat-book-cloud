package fun.amireux.chat.book.auth.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record CaptchaLoginRequest(
        @Schema(description = "邮箱")
        String email,
        @Schema(description = "验证码")
        String captcha
) {
}
