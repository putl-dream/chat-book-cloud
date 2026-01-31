package com.putl.userservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "登录")
public class LoginVO {
    @Schema(description = "邮箱")
    private String email;
    @Schema(description = "密码")
    private String password;
}
