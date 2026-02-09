package fun.amireux.chat.book.framework.common.pojo;

public interface ErrorType {
    ErrorCode ERROR_404 = new ErrorCode(404, "404 NOT FOUND");
    ErrorCode ERROR_500 = new ErrorCode(500, "500 INTERNAL SERVER ERROR");
    ErrorCode ERROR_400 = new ErrorCode(400, "400 BAD REQUEST");
    ErrorCode ERROR_401 = new ErrorCode(401, "用户信息未找到，请重新登录");
    ErrorCode ERROR_403 = new ErrorCode(403, "403 FORBIDDEN");
}
