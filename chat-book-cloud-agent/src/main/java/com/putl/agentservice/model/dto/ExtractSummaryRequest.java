package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "正文摘要提取请求")
public class ExtractSummaryRequest {

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "正文内容")
    private String content;
}
