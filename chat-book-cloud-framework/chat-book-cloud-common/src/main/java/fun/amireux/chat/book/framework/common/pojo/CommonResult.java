package fun.amireux.chat.book.framework.common.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
public class CommonResult<T> implements Serializable {

    private Integer code;

    private T data;

    private String msg;

    public static <T> CommonResult<T> error(CommonResult<?> result) {
        return error(result.getCode(), result.getMsg());
    }

    public static <T> CommonResult<T> error(Integer code, String message) {
        CommonResult<T> result = new CommonResult<>();
        result.code = code;
        result.msg = message;
        return result;
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode) {
        return error(errorCode.code(), errorCode.message());
    }

    public static <T> CommonResult<T> error(ErrorCode errorCode, String messageFormat, Object... params) {
        return error(errorCode.code(), String.format(messageFormat, params));
    }


    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<>();
        result.code = 200;
        result.data = data;
        result.msg = "";
        return result;
    }

    public static <T> CommonResult<T> success() {
        CommonResult<T> result = new CommonResult<>();
        result.code = 200;
        result.data = null;
        result.msg = "";
        return result;
    }

    public static boolean isSuccess(Integer code) {
        return Objects.equals(code, 200);
    }

    @JsonIgnore
    public boolean isSuccess() {
        return isSuccess(code);
    }

    @JsonIgnore
    public boolean isError() {
        return !isSuccess();
    }
}
