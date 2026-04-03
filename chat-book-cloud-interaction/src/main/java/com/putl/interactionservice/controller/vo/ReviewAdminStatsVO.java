package com.putl.interactionservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论治理统计")
public class ReviewAdminStatsVO {
    @Schema(description = "评论总数")
    private Long totalCount;

    @Schema(description = "正常评论数")
    private Long normalCount;

    @Schema(description = "已屏蔽评论数")
    private Long hiddenCount;

    @Schema(description = "已删除评论数")
    private Long deletedCount;

    @Schema(description = "异常评论数（已屏蔽 + 已删除）")
    private Long abnormalCount;
}
