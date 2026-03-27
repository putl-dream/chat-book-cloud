package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "生成草稿请求")
public class GenerateDraftRequest {

    @Schema(description = "会话 ID")
    private Integer sessionId;
}
