package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章列表信息对象")
public class ArticleListVO implements Serializable {
    @Schema(description = "文章ID")
    private Integer id;

    @Schema(description = "作者ID")
    private Integer userId;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "文章封面图片URL")
    @JsonSerialize(using = FileUrlSerializer.class)
    private String cover;

    @Schema(description = "文章摘要")
    private String abstractText;

    @Schema(description = "作者名称")
    private String userName;

    @Schema(description = "作者头像图片URL")
    @JsonSerialize(using = FileUrlSerializer.class)
    private String authorAvatar;

    @Schema(description = "文章分类ID")
    private Integer category;

    @Schema(description = "内容类型：0-学习/教程 1-实战/项目")
    private Integer contentType;

    @Schema(description = "标签ID列表")
    private List<Integer> tagIds;

    @Schema(description = "文章状态：0-草稿 1-待审核 2-已发布 -1-已删除")
    private Integer status;

    @Schema(description = "点赞数量")
    private Long praiseCount;

    @Schema(description = "评论数量")
    private Long commentCount;

    @Schema(description = "浏览数量")
    private Long viewCount;

    @Schema(description = "收藏数量")
    private Long collectCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "文章创建时间")
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Schema(description = "文章更新时间")
    private LocalDateTime updateTime;
}

