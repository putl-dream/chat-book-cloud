package com.putl.userservice.controller.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import fun.amireux.chat.book.minio.jackson.FileUrlSerializer;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
