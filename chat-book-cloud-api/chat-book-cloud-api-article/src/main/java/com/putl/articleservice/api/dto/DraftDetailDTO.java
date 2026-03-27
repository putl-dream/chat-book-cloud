package com.putl.articleservice.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DraftDetailDTO {
    private Integer draftId;
    private Integer userId;
    private Integer sourceSessionId;
    private String title;
    private String summary;
    private String content;
    private Integer currentVersionNo;
    private String status;
}
