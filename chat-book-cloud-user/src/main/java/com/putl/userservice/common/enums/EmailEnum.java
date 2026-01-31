package com.putl.userservice.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EmailEnum {
    TITLE("【iec】验证码"),
    CAPTCHA("[iec]邮箱验证码，你的邮箱验证码为："),
    THANKS("，感谢使用iec！");
    private final String desc;
}
