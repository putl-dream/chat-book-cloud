package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 内容类型分页请求参数
 */
@Data
@Schema(description = "内容类型分页请求参数")
public class ContentTypePageRequestDTO extends PageRequestDTO {

    @Schema(description = "内容类型：0-学习/教程 1-实战/项目")
    private Integer contentType;
}
