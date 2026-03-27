package com.putl.articleservice.api.dto;

import lombok.Data;

@Data
public class CreateDraftRequest {
    private Integer userId;
    private Integer sourceSessionId;
    private String title;
    private String summary;
    private String content;
    private String sourceType;
    private String instruction;
}
