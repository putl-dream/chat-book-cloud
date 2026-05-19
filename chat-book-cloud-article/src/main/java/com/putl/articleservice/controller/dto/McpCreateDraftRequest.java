package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "MCP 创建草稿请求")
public class McpCreateDraftRequest {

    @Schema(description = "草稿标题")
    private String title;

    @Schema(description = "草稿摘要")
    private String summary;

    @Schema(description = "草稿正文，建议使用 Markdown")
    private String content;

    @Schema(description = "来源说明或生成指令")
    private String instruction;
}
