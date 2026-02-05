package fun.amireux.chat.book.auth.service.dto;

import fun.amireux.chat.book.auth.projectobject.LoginMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDTO {
    @Schema(description = "鉴权方式")
    LoginMethod loginMethod;
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "验证码")
    private String captcha;

    public String getVerificationCode() {
        return captcha;
    }

    public void setVerificationCode(String captcha) {
        this.captcha = captcha;
    }
}
