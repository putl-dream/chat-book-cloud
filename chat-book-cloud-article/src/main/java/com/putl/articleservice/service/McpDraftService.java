package com.putl.articleservice.service;

import com.putl.articleservice.controller.dto.McpCreateDraftRequest;
import com.putl.articleservice.controller.vo.McpCreateDraftResponse;
import com.putl.articleservice.model.McpPrincipal;

public interface McpDraftService {

    McpCreateDraftResponse createDraft(McpPrincipal principal, McpCreateDraftRequest request);
}
