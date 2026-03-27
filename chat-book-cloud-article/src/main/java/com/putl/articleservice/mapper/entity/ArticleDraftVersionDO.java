package com.putl.articleservice.mapper.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("article_draft_version")
public class ArticleDraftVersionDO {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer draftId;

    private Integer versionNo;

    private String sourceType;

    private String instruction;

    private String title;

    private String summary;

    private String content;

    private Integer adopted;

    private LocalDateTime createTime;
}
