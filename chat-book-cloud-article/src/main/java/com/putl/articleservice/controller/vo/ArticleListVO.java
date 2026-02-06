package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章列表信息对象")
public class ArticleListVO implements Serializable {
    @Schema(description = "文章ID")
    private Integer id;

    @Schema(description = "文章标题")
    private String title;

    @JsonProperty("cover")
    @Schema(description = "文章封面图片URL")
    private String articleCover;

    @JsonProperty("abstractText")
    @Schema(description = "文章摘要")
    private String summary;

    @Schema(description = "作者名称")
    private String userName;

    @Schema(description = "作者头像图片URL")
    private String authorAvatar;

    @Schema(description = "文章分类ID")
    private Integer category;

    @Schema(description = "点赞数量")
    private Long praiseCount;

    @Schema(description = "评论数量")
    private Long commentCount;

    @Schema(description = "浏览数量")
    private Long viewCount;

    @Schema(description = "收藏数量")
    private Long collectCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "文章创建时间")
    private LocalDateTime createTime;
}

