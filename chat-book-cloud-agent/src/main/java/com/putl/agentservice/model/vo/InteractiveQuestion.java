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
@Schema(description = "交互式问题")
public class InteractiveQuestion {

    @Schema(description = "问题 ID")
    private String id;

    @Schema(description = "问题标题")
    private String label;

    @Schema(description = "题型：single_choice / multi_choice / text_input")
    private String type;

    @Schema(description = "是否必填")
    private Boolean required;

    @Schema(description = "是否允许自定义输入")
    private Boolean allowCustomInput;

    @Schema(description = "输入占位提示")
    private String placeholder;

    @Schema(description = "候选选项")
    private List<InteractiveOption> options;
}
