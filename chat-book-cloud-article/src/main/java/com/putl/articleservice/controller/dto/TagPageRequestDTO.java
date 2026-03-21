package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 标签分页请求参数
 */
@Data
@Schema(description = "标签分页请求参数")
public class TagPageRequestDTO extends PageRequestDTO {

    @Schema(description = "标签类型：1-技术栈 2-学习路径")
    private Integer type;

    @Schema(description = "标签ID")
    private Integer tagId;
}
