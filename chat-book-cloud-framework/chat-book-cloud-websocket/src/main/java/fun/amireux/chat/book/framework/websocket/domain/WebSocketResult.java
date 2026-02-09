package fun.amireux.chat.book.framework.websocket.domain;

import fun.amireux.chat.book.framework.common.utils.BeanUtil;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WebSocketResult<T> {
    private String type;
    private T data;

    /**
     * 构建通用响应并转换为 JSON 字符串
     */
    public static <T> String of(String type, T data) {
        return BeanUtil.toJsonString(new WebSocketResult<>(type, data));
    }
    
    /**
     * 系统消息
     */
    public static String system(String message) {
        return of("SYSTEM", message);
    }

    /**
     * 错误消息
     */
    public static String error(String message) {
        return of("ERROR", message);
    }
}
