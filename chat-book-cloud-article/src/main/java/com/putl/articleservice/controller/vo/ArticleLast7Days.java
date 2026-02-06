package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "最近7天文章统计信息")
public class ArticleLast7Days {
    @Schema(description = "最近7天每天的文章数量")
    private List<Long> articleCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "最近7天每天的日期")
    private List<LocalDateTime> reviewCount;
}
