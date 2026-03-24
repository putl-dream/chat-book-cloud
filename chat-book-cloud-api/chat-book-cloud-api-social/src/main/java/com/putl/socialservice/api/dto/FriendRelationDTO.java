package com.putl.socialservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 好友关系枚举
 */
@Getter
@AllArgsConstructor
public enum FriendRelationDTO {
    Not_Friend(-1, "陌生人"),
    Follow(0, "已关注"),
    Friend(1, "互相关注");

    private final Integer code;
    private final String description;
}
