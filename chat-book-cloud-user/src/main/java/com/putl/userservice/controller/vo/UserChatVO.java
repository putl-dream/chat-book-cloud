package com.putl.userservice.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.putl.userservice.util.CustomLocalDateTimeSerializer;
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
    @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
    private LocalDateTime lastTime;
}
