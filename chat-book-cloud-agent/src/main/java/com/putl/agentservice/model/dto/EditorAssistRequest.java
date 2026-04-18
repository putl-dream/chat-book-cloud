package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "编辑器 AI 辅助请求")
public class EditorAssistRequest {

    @Schema(description = "任务模式，CONTINUE / EDIT")
    private String mode;

    @Schema(description = "应用目标，INSERT_AT_CURSOR / REPLACE_SELECTION / APPEND_TO_END")
    private String applyTarget;

    @Schema(description = "用户指令")
    private String instruction;

    @Schema(description = "当前标题")
    private String title;

    @Schema(description = "当前摘要")
    private String summary;

    @Schema(description = "当前正文纯文本")
    private String content;

    @Schema(description = "当前选中文本")
    private String selectedText;

    @Schema(description = "最近对话消息")
    private List<EditorAssistMessage> history;
}
