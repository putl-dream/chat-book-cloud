package com.putl.interactionservice.api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Schema(description = "Review list item")
public class ReviewListVO {
    private Integer id;
    private Integer articleId;
    private String content;
    private Integer parentId;
    private String username;
    private String headerImg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<ReviewListVO> children;
}
