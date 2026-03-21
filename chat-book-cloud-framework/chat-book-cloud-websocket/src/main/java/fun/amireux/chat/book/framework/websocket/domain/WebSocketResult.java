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
    private String requestId; // 请求唯一标识，用于 ACK 追踪

    /**
     * 构建通用响应并转换为 JSON 字符串
     */
    public static <T> String of(String type, T data) {
        return BeanUtil.toJsonString(new WebSocketResult<>(type, data, null));
    }

    /**
     * 构建带 requestId 的响应并转换为 JSON 字符串
     */
    public static <T> String of(String type, T data, String requestId) {
        return BeanUtil.toJsonString(new WebSocketResult<>(type, data, requestId));
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
