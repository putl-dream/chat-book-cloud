package com.putl.articleservice.utils;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "统一响应结果")
public class ImageResult<T> {
    private Integer errno;
    private Integer code;
    private String message;
    private T data;

    public static <T> ImageResult<T> success(T data) {
        return new ImageResult<>(0,200,"success", data);
    }

    public static <T> ImageResult<T> error(String msg){
        return new ImageResult<>(1, 500,msg, null);
    }
}