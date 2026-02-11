package com.putl.articleservice.client.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "文章数据信息")
public class UserFootVO {
    @Schema(description = "文章id")
    private Integer articleId;
    @Schema(description = "用户id")
    private Integer userId;
    @Schema(description = "点赞类型")
    private Integer praiseStat;
    @Schema(description = "收藏类型")
    private Integer collectStat;
    @Schema(description = "阅读量")
    private Long viewCount;
}
