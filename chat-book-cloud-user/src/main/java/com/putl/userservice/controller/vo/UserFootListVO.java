package com.putl.userservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文章列表数据信息")
public class UserFootListVO {
    @Schema(description = "文章id")
    private Integer articleId;
    @Schema(description = "作者id")
    private Integer authorId;
    @Schema(description = "点赞数量")
    private Long  praiseCount;
    @Schema(description = "评论数量")
    private Long commentCount;
    @Schema(description = "浏览数量")
    private Long viewCount;
    @Schema(description = "收藏数量")
    private Long collectCount;
}
