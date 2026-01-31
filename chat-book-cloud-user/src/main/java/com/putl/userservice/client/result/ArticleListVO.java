package com.putl.userservice.client.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "ArticleListVO")
public class ArticleListVO {
    @Schema(description = "文章id")
    private Integer id;
    private String title;
    private String articleCover;
    private String summary;
    private String author;
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
    private String createTime;
}