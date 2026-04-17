package com.putl.agentservice.model.vo;

import com.putl.agentservice.enums.AgentAssistantAction;
import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.DraftReadiness;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "草稿生成响应")
public class DraftGenerateResponse {

    @Schema(description = "草稿 ID")
    private Integer draftId;

    @Schema(description = "版本号")
    private Integer versionNo;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "当前执行场景")
    private AgentSceneType currentScene;

    @Schema(description = "建议的下一场景")
    private AgentSceneType nextScene;

    @Schema(description = "助手动作")
    private AgentAssistantAction assistantAction;

    @Schema(description = "首稿准备度")
    private DraftReadiness draftReadiness;
}
