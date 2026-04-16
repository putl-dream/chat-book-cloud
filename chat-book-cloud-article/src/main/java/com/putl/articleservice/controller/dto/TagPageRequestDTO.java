package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签分页请求参数
 */
@Data
@Schema(description = "标签分页请求参数")
public class TagPageRequestDTO extends PageRequestDTO {

    @Schema(description = "作者标签名称")
    private String authorTagName;
}
