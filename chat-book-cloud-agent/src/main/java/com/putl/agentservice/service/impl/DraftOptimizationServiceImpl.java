package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.model.dto.AdoptDraftVersionRequest;
import com.putl.agentservice.model.dto.OptimizeDraftRequest;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.DraftOptimizeResponse;
import com.putl.agentservice.service.DraftOptimizationService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import org.springframework.stereotype.Service;

@Service
public class DraftOptimizationServiceImpl implements DraftOptimizationService {

    private final ArticleAiGateway articleAiGateway;
    private final ArticleClient articleClient;

    public DraftOptimizationServiceImpl(ArticleAiGateway articleAiGateway, ArticleClient articleClient) {
        this.articleAiGateway = articleAiGateway;
        this.articleClient = articleClient;
    }

    @Override
    public DraftOptimizeResponse optimizeDraft(OptimizeDraftRequest request) {
        DraftDetailDTO draft = articleClient.getDraftDetail(request.getDraftId()).getData();
        ArticleDraftResult optimized = articleAiGateway.optimizeDraft(
                request.getInstruction(),
                draft.getTitle(),
                draft.getSummary(),
                draft.getContent());

        CreateDraftVersionRequest versionRequest = new CreateDraftVersionRequest();
        versionRequest.setDraftId(request.getDraftId());
        versionRequest.setTitle(optimized.getTitle());
        versionRequest.setSummary(optimized.getSummary());
        versionRequest.setContent(optimized.getContent());
        versionRequest.setSourceType("OPTIMIZE");
        versionRequest.setInstruction(request.getInstruction());

        CreateDraftResponse response = articleClient.createDraftVersion(versionRequest).getData();
        return DraftOptimizeResponse.builder()
                .draftId(response.getDraftId())
                .candidateVersionNo(response.getVersionNo())
                .build();
    }

    @Override
    public void adoptVersion(AdoptDraftVersionRequest request) {
        DraftVersionAdoptRequest adoptRequest = new DraftVersionAdoptRequest();
        adoptRequest.setDraftId(request.getDraftId());
        adoptRequest.setVersionNo(request.getVersionNo());
        articleClient.adoptDraftVersion(adoptRequest);
    }
}
