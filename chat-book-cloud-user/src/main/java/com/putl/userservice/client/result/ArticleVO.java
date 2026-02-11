package com.putl.userservice.client.result;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ArticleVO")
public class ArticleVO {
    private Integer id;
    private String title;
    private String userName;
    private Integer userId;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;
    
    private String content;
    private String cover;
    private Integer category;
    private String abstractText;
    private Integer status;
    
    private Integer praiseStat;
    private Integer collectStat;
    private Long viewCount;
}