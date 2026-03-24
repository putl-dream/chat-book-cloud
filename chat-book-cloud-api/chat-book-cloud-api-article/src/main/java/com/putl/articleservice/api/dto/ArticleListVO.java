package com.putl.articleservice.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ArticleListVO")
public class ArticleListVO {
    @Schema(description = "文章id")
    private Integer id;
    private String title;
    private String cover;
    private String abstractText;
    private String userName;
    private String authorAvatar;
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
    private LocalDateTime createTime;
}
