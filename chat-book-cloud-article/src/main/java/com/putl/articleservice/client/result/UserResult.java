package com.putl.articleservice.client.result;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserResult {
    private Integer id;
    private String username;
    private String email;
    private String photo;
    private String profile;
    private String role;
}
