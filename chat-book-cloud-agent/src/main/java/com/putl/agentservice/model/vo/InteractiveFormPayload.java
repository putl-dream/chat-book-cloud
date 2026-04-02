package com.putl.agentservice.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "交互式表单消息体")
public class InteractiveFormPayload {

    @Schema(description = "表单 ID")
    private String formId;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "说明文案")
    private String description;

    @Schema(description = "提交模式，当前固定 batch")
    private String submitMode;

    @Schema(description = "问题列表")
    private List<InteractiveQuestion> questions;
}
