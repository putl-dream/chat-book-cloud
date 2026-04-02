package com.putl.agentservice.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "交互式表单回答")
public class InteractionResponseRequest {

    @Schema(description = "表单 ID")
    private String formId;

    @Schema(description = "表单标题")
    private String title;

    @Schema(description = "表单说明")
    private String description;

    @Schema(description = "答案列表")
    private List<InteractionAnswerRequest> answers;
}
