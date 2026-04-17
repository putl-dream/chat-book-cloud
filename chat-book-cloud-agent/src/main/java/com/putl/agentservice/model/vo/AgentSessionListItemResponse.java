package com.putl.agentservice.model.vo;

import com.putl.agentservice.enums.AgentSceneType;
import com.putl.agentservice.enums.AgentSessionStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "历史会话列表项")
public class AgentSessionListItemResponse {

    @Schema(description = "会话 ID")
    private Integer id;

    @Schema(description = "会话标题")
    private String title;

    @Schema(description = "当前场景")
    private AgentSceneType sceneType;

    @Schema(description = "会话状态")
    private AgentSessionStatus status;

    @Schema(description = "关联草稿 ID")
    private Integer targetDraftId;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "最近更新时间")
    private LocalDateTime updateTime;
}
