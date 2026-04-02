package com.putl.interactionservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "评论参数")
public class ReviewVO {
    @Schema(description = "评论ID")
    private Integer id;

    @Schema(description = "文章id")
    private Integer articleId;

    @Schema(description = "用户ID")
    private Integer userId;

    @Schema(description = "父评论id")
    private Integer parentId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论状态：0-正常，1-已删除，2-已屏蔽")
    private Integer status;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "用户头像")
    private String headerImg;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "评论时间")
    private LocalDateTime createTime;
}
