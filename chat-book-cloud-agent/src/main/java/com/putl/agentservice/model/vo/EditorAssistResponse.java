package com.putl.agentservice.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "编辑器 AI 辅助响应")
public class EditorAssistResponse {

    @Schema(description = "给用户看的简短说明")
    private String reply;

    @Schema(description = "可直接应用到正文的 Markdown 内容")
    private String result;
}
