package com.putl.socialservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注统计数据DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowStatDTO {
    /**
     * 关注数
     */
    private Integer followCount;

    /**
     * 粉丝数
     */
    private Integer fanCount;

    /**
     * 好友数（互相关注）
     */
    private Integer friendCount;
}
