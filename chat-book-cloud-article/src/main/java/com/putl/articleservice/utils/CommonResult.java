package com.putl.articleservice.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommonResult<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> CommonResult<T> success(T data) {
        return new CommonResult<T>(200, "success", data);
    }

    public static <T> CommonResult<T> success(T data, String message) {
        return new CommonResult<T>(200, message, data);
    }

    public static <T> CommonResult<T> failed(String message) {
        return new CommonResult<T>(500, message, null);
    }

    public static <T> CommonResult<T> failed(int code,String message) {
        return new CommonResult<T>(code, message, null);
    }

    public static <T> CommonResult<T> failed() {
        return failed("failed");
    }
}
