package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 相关推荐请求参数
 */
@Data
@Schema(description = "相关推荐请求参数")
public class RelatedPageRequestDTO extends PageRequestDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID")
    private Integer articleId;
}