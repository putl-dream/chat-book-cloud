package com.putl.userservice.api.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserChatVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String photo;
    private String profile;
    private String lastChat;
    private LocalDateTime lastTime;
}
