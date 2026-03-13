package com.putl.interactionservice.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "Notification message")
public class NotificationVO {
    @Schema(description = "Notification record ID")
    private Integer id;
    @Schema(description = "Sender user ID")
    private Integer senderId;
    @Schema(description = "Action type: PRAISE/COLLECT/COMMENT/BROWSE")
    private String actionType;
    @Schema(description = "Article ID")
    private Integer articleId;
    @Schema(description = "Article title")
    private String articleTitle;
    @Schema(description = "Create time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
}
