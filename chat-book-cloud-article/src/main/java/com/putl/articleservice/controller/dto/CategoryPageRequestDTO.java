package com.putl.articleservice.controller.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分类分页请求参数校验DTO
 *
 * @since 2026-01-31
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryPageRequestDTO extends PageRequestDTO {

    /**
     * 分类ID
     */
    @NotNull(message = "分类ID不能为空")
    private Integer category;
}
