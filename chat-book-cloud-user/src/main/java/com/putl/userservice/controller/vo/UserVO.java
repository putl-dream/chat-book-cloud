package com.putl.userservice.controller.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserVO {
    private Integer id;
    private Integer userId;
    private String username;
    private String email;
    private String photo;
    private String profile;
    private String role;
}
