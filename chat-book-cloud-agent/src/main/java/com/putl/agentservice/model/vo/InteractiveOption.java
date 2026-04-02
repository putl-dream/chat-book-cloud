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
@Schema(description = "交互式问题选项")
public class InteractiveOption {

    @Schema(description = "选项文案")
    private String label;

    @Schema(description = "选项值")
    private String value;

    @Schema(description = "选项补充说明")
    private String description;
}
