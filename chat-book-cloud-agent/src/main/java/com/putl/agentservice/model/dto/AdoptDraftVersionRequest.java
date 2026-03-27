package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "采用草稿版本请求")
public class AdoptDraftVersionRequest {

    @Schema(description = "草稿 ID")
    private Integer draftId;

    @Schema(description = "版本号")
    private Integer versionNo;
}
