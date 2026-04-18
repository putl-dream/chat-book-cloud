package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "编辑器 AI 会话消息")
public class EditorAssistMessage {

    @Schema(description = "消息角色，user / assistant")
    private String role;

    @Schema(description = "消息内容")
    private String content;
}
