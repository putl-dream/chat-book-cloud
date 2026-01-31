package com.putl.userservice.util;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.http.HttpStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data) {
        return new Result<>(HttpStatus.SC_OK, "success", data);
    }

    public static <T> Result<T> success() {
        return new Result<>(HttpStatus.SC_OK, "success", null);
    }

    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(HttpStatus.SC_OK, msg, data);
    }

    public static <T> Result<T> error(String msg) {
        return new Result<>(HttpStatus.SC_BAD_REQUEST, msg, null);
    }

    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }
}
