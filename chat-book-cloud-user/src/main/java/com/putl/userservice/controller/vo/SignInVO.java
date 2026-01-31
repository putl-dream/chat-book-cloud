package com.putl.userservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "注册")
public class SignInVO {
    @Schema(description = "用户名")
    private String username;
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "密码")
    private String password;
    @Schema(description = "验证码")
    private String captcha;
}
