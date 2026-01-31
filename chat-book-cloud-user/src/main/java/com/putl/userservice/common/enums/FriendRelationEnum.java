package com.putl.userservice.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FriendRelationEnum {
    Not_Friend(-1),

    Follow(0),

    Friend(1);

    private final Integer code;
}
