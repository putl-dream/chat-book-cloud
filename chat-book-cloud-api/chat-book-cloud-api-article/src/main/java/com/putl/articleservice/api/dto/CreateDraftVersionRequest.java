package com.putl.articleservice.api.dto;

import lombok.Data;

@Data
public class CreateDraftVersionRequest {
    private Integer draftId;
    private String title;
    private String summary;
    private String content;
    private String sourceType;
    private String instruction;
}
