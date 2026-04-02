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
@Schema(description = "Agent 结构化回复")
public class AgentAssistantMessage {

    @Schema(description = "消息类型，text / interactive_form")
    private String messageType;

    @Schema(description = "文本内容")
    private String content;

    @Schema(description = "交互式表单载荷")
    private InteractiveFormPayload payload;
}
