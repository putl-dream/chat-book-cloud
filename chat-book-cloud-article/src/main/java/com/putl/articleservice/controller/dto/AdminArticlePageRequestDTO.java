package com.putl.articleservice.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 管理员文章分页查询请求
 */
@Data
@Builder
@Schema(description = "管理员文章分页查询请求")
public class AdminArticlePageRequestDTO {
    @Schema(description = "页码", defaultValue = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页条数", defaultValue = "10")
    private Integer pageSize = 10;

    @Schema(description = "文章状态筛选：0-草稿 1-待审核 2-已发布 -1-已删除，null表示不限")
    private Integer status;

    @Schema(description = "分类ID筛选")
    private Integer category;

    @Schema(description = "内容类型筛选：0-学习/教程 1-实战/项目")
    private Integer contentType;

    @Schema(description = "作者用户ID筛选")
    private Integer userId;

    @Schema(description = "关键词（搜索标题/摘要）")
    private String keyword;

    @Schema(description = "排序方向：desc（默认）或 asc")
    private String orderDirection = "desc";
}
