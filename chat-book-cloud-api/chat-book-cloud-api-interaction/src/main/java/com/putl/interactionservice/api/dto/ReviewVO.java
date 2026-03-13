package com.putl.interactionservice.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Review request")
public class ReviewVO {
    @Schema(description = "Article id")
    private Integer articleId;
    @Schema(description = "Parent review id")
    private Integer parentId;
    @Schema(description = "Review content")
    private String content;
}
