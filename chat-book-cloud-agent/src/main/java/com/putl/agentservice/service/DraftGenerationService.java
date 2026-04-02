package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.vo.DraftGenerateResponse;

public interface DraftGenerationService {

    DraftGenerateResponse generateDraft(GenerateDraftRequest request);

    void generateDraftByWebSocket(String userId, GenerateDraftRequest request);
}
