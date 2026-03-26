package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.putl.articleservice.enums.ArticleReviewAction;
import com.putl.articleservice.enums.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "管理员审核结果")
public class ArticleReviewResultVO {

    @Schema(description = "文章ID")
    private Integer articleId;

    @Schema(description = "文章当前状态")
    private ArticleStatus status;

    @Schema(description = "审核动作")
    private ArticleReviewAction reviewAction;

    @Schema(description = "审核原因/备注")
    private String reviewReason;

    @Schema(description = "审核人ID")
    private Integer reviewerId;

    @Schema(description = "审核人用户名")
    private String reviewerName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "审核完成时间")
    private LocalDateTime reviewedAt;

    @Schema(description = "批量审核批次号，单条审核时为空")
    private String batchId;
}
