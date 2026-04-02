package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "交互式问题答案")
public class InteractionAnswerRequest {

    @Schema(description = "问题 ID")
    private String questionId;

    @Schema(description = "问题文案")
    private String questionLabel;

    @Schema(description = "问题类型")
    private String questionType;

    @Schema(description = "答案值，支持字符串或数组")
    private Object value;
}
