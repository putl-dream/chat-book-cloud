package com.putl.articleservice.controller.vo;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.putl.articleservice.enums.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章详细信息对象")
public class ArticleVO {
    @Schema(description = "文章ID")
    private Integer id;

    @NotNull
    @Schema(description = "文章标题")
    private String title;

    @NotNull
    @Schema(description = "作者用户名")
    private String userName;

    @NotNull
    @Schema(description = "作者ID")
    private Integer userId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "文章创建时间")
    private LocalDateTime createTime;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章封面")
    private String cover;

    @Schema(description = "文章分类")
    private Integer category;

    @Schema(description = "文章摘要")
    private String abstractText;

    @Schema(description = "文章状态")
    private ArticleStatus status;

    @Schema(description = "点赞类型")
    private Integer praiseStat;

    @Schema(description = "收藏类型")
    private Integer collectStat;

    @Schema(description = "阅读数量")
    private Long viewCount;
}
