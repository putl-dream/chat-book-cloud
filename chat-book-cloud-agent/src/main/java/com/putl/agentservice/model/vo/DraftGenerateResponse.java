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
@Schema(description = "草稿生成响应")
public class DraftGenerateResponse {

    @Schema(description = "草稿 ID")
    private Integer draftId;

    @Schema(description = "版本号")
    private Integer versionNo;
}
