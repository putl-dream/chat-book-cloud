package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "优化草稿请求")
public class OptimizeDraftRequest {

    @Schema(description = "草稿 ID")
    private Integer draftId;

    @Schema(description = "优化指令")
    private String instruction;
}
