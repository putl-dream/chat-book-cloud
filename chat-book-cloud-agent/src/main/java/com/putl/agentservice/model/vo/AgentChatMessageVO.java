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
@Schema(description = "聊天消息")
public class AgentChatMessageVO {

    @Schema(description = "消息 ID")
    private Integer id;

    @Schema(description = "消息角色")
    private String role;

    @Schema(description = "消息类型")
    private String messageType;

    @Schema(description = "消息文本")
    private String content;

    @Schema(description = "结构化载荷")
    private Object payload;

    @Schema(description = "创建时间")
    private Object createTime;
}
