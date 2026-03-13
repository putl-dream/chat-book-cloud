package com.putl.interactionservice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "通知消息VO")
public class NotificationVO {
    @Schema(description = "通知记录ID")
    private Integer id;
    @Schema(description = "触发通知的用户ID（操作人）")
    private Integer senderId;
    @Schema(description = "操作类型：PRAISE=点赞 | COLLECT=收藏 | COMMENT=评论 | BROWSE=浏览")
    private String actionType;
    @Schema(description = "被操作的文章ID")
    private Integer articleId;
    @Schema(description = "被操作的文章标题")
    private String articleTitle;
    @Schema(description = "通知发生时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
