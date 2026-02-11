package com.putl.userservice.api.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResult {
    private Integer id;
    private String username;
    private String email;
    private String photo;
    private String profile;
    private String role;
}
