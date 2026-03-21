package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 多条件筛选分页请求参数
 */
@Data
@Schema(description = "多条件筛选分页请求参数")
public class MultiFilterPageRequestDTO extends PageRequestDTO {

    @Schema(description = "内容类型：0-学习/教程 1-实战/项目")
    private Integer contentType;

    @Schema(description = "技术分类")
    private Integer category;

    @Schema(description = "标签ID")
    private Integer tagId;
}
