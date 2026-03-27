package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.AdoptDraftVersionRequest;
import com.putl.agentservice.model.dto.OptimizeDraftRequest;
import com.putl.agentservice.model.vo.DraftOptimizeResponse;

public interface DraftOptimizationService {

    DraftOptimizeResponse optimizeDraft(OptimizeDraftRequest request);

    void adoptVersion(AdoptDraftVersionRequest request);
}
