package com.putl.interactionservice.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ReviewListVO {
    private Integer id;
    private Integer articleId;
    private String content;
    private Integer parentId;
    private String username;
    @JsonSerialize(using = FileUrlSerializer.class)
    private String headerImg;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    private List<ReviewListVO> children;
}
