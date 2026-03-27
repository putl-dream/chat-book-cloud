package com.putl.userservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "角色更新请求")
public class UpdateRoleRequest {
    @NotBlank(message = "角色不能为空")
    @Schema(description = "目标角色：USER 或 ADMIN")
    private String role;
}
