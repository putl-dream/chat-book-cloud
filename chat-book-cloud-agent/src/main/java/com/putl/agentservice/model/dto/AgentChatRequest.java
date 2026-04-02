package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Agent 聊天请求")
public class AgentChatRequest {

    @Schema(description = "会话 ID")
    private Integer sessionId;

    @Schema(description = "消息内容")
    private String content;

    @Schema(description = "交互式表单回答")
    private InteractionResponseRequest interactionResponse;
}
