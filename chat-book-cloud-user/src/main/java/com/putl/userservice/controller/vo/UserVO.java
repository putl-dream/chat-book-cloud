package com.putl.userservice.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.putl.userservice.config.FileUrlSerializer;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    @JsonSerialize(using = FileUrlSerializer.class)
    private String photo;
    private String profile;
    private String role;
}
