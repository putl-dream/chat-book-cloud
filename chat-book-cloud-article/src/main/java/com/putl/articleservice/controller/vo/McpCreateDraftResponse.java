package com.putl.articleservice.controller.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class McpCreateDraftResponse {

    private Integer draftId;

    private Integer versionNo;

    private String status;
}
