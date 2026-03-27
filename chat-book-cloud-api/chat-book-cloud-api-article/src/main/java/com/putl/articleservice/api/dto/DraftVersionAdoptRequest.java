package com.putl.articleservice.api.dto;

import lombok.Data;

@Data
public class DraftVersionAdoptRequest {
    private Integer draftId;
    private Integer versionNo;
}
