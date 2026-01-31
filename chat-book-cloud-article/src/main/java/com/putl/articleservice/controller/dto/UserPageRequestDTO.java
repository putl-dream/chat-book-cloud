package com.putl.articleservice.controller.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户分页请求参数校验DTO
 *
 * @since 2026-01-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserPageRequestDTO extends PageRequestDTO {

    /**
     * 用户ID
     */
    @NotNull(message = "用户ID不能为空")
    private Integer userId;
}
