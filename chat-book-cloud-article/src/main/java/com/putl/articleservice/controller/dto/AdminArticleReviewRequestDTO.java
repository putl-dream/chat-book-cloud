package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员文章审核请求")
public class AdminArticleReviewRequestDTO {

    @NotNull(message = "文章ID不能为空")
    @Schema(description = "文章ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer articleId;

    @Size(max = 255, message = "审核原因不能超过255个字符")
    @Schema(description = "审核原因/备注，驳回时必填")
    private String reason;
}
