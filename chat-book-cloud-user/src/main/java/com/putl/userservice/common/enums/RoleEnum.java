package com.putl.userservice.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum RoleEnum {
    USER(0, "普通用户"),
    ADMIN(1, "管理员");

    @EnumValue
    @JsonValue
    private final Integer code;
    private final String desc;
}
