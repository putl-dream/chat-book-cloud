package com.putl.articleservice.controller.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
import com.putl.articleservice.enums.ArticleStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "文章详细信息对象")
public class ArticleVO {
    @Schema(description = "文章ID")
    private Integer id;

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "作者用户名快照")
    private String userName;

    @Schema(description = "作者ID")
    private Integer userId;

    @Schema(description = "作者头像")
    private String authorAvatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "文章创建时间")
    private LocalDateTime createTime;

    @Schema(description = "文章内容")
    private String content;

    @Schema(description = "文章封面")
    @JsonSerialize(using = FileUrlSerializer.class)
    private String cover;

    @Schema(description = "文章分类")
    private Integer category;

    @Schema(description = "内容类型：0-学习/教程 1-实战/项目")
    private Integer contentType;

    @Schema(description = "文章标签ID列表")
    private List<Integer> tagIds;

    @Schema(description = "文章摘要")
    private String abstractText;

    @Schema(description = "文章状态")
    private ArticleStatus status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "最近一次更新时间，用于乐观校验")
    private LocalDateTime updatedAt;

    @Schema(description = "点赞类型")
    private Integer praiseStat;

    @Schema(description = "收藏类型")
    private Integer collectStat;

    @Schema(description = "阅读数量")
    private Long viewCount;
}
