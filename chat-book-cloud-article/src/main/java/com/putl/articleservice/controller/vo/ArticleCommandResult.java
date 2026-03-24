package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
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
@Schema(description = "文章命令执行结果")
public class ArticleCommandResult {
    @Schema(description = "文章ID")
    private Integer articleId;

    @Schema(description = "文章状态")
    private ArticleStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近一次更新时间")
    private LocalDateTime updatedAt;
}
