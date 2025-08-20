package fun.amireux.chat.book.framework.websocket.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private MsgType type;
    private String from;
    private String to;
    private String content;



}

