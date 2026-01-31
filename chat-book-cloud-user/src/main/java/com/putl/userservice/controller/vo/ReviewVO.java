package com.putl.userservice.controller.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "评论参数")
public class ReviewVO {
    @Schema(description = "文章id")
    private Integer articleId;
    @Schema(description = "父评论id")
    private Integer parentId;
    @Schema(description = "评论内容")
    private String content;
}