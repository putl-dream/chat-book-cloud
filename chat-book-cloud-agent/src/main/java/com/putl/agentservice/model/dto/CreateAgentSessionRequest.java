package com.putl.agentservice.model.dto;

import com.putl.agentservice.enums.AgentSceneType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建 Agent 会话请求")
public class CreateAgentSessionRequest {

    @Schema(description = "场景类型")
    private AgentSceneType sceneType;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "关联文章 ID")
    private Integer targetArticleId;

    @Schema(description = "关联草稿 ID")
    private Integer targetDraftId;
}
