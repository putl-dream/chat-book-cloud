package fun.amireux.chat.book.framework.common.exceptions;

import fun.amireux.chat.book.framework.common.pojo.ErrorCode;
import lombok.*;

/**
 * 服务器异常 Exception
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public final class ServiceException extends RuntimeException {

    private Integer code;

    private String message;

    public ServiceException(ErrorCode errorCode) {
        this.code = errorCode.code();
        this.message = errorCode.message();
    }

    @Override
    public String getMessage() {
        return message;
    }
}
