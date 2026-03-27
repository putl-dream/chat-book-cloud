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
@Schema(description = "草稿优化响应")
public class DraftOptimizeResponse {

    @Schema(description = "草稿 ID")
    private Integer draftId;

    @Schema(description = "候选版本号")
    private Integer candidateVersionNo;
}
