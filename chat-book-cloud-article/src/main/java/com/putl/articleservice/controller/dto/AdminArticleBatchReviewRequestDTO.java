package com.putl.articleservice.controller.dto;

import com.putl.articleservice.enums.ArticleReviewAction;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员批量审核请求")
public class AdminArticleBatchReviewRequestDTO {

    @NotEmpty(message = "文章ID列表不能为空")
    @Schema(description = "待审核文章ID列表", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<Integer> articleIds;

    @NotNull(message = "审核动作不能为空")
    @Schema(description = "审核动作", requiredMode = Schema.RequiredMode.REQUIRED)
    private ArticleReviewAction action;

    @Size(max = 255, message = "审核原因不能超过255个字符")
    @Schema(description = "审核原因/备注，批量驳回时必填")
    private String reason;
}
