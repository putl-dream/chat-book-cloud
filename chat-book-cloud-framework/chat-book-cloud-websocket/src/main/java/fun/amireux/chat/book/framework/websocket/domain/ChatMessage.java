package fun.amireux.chat.book.framework.websocket.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ChatMessage extends BaseMessage {
    public ChatMessage() {
        super("chat");
    }

    private String from;
    private String to;
    private String content;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime time;
    private String msgType;
}
