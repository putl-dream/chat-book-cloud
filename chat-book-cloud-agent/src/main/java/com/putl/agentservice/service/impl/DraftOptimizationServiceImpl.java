package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.ArticleAiGateway;
import com.putl.agentservice.model.dto.AdoptDraftVersionRequest;
import com.putl.agentservice.model.dto.ExtractSummaryRequest;
import com.putl.agentservice.model.dto.OptimizeDraftRequest;
import com.putl.agentservice.model.vo.AiInvocationResult;
import com.putl.agentservice.model.vo.ArticleDraftResult;
import com.putl.agentservice.model.vo.ArticleSummaryResponse;
import com.putl.agentservice.model.vo.DraftOptimizeResponse;
import com.putl.agentservice.service.DraftOptimizationService;
import com.putl.articleservice.api.ArticleClient;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
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
        CommonResult<DraftDetailDTO> draftResult = articleClient.getDraftDetail(request.getDraftId());
        if (draftResult == null || !draftResult.isSuccess() || draftResult.getData() == null) {
            throw new IllegalStateException("Failed to load draft detail from article service");
        }
        DraftDetailDTO draft = draftResult.getData();
        AiInvocationResult<ArticleDraftResult> optimized = articleAiGateway.optimizeDraft(
                request.getInstruction(),
                draft.getTitle(),
                draft.getSummary(),
                draft.getContent());

        CreateDraftVersionRequest versionRequest = new CreateDraftVersionRequest();
        versionRequest.setDraftId(request.getDraftId());
        versionRequest.setTitle(optimized.getData().getTitle());
        versionRequest.setSummary(optimized.getData().getSummary());
        versionRequest.setContent(optimized.getData().getContent());
        versionRequest.setSourceType("OPTIMIZE");
        versionRequest.setInstruction(request.getInstruction());

        CommonResult<CreateDraftResponse> versionResult = articleClient.createDraftVersion(versionRequest);
        if (versionResult == null || !versionResult.isSuccess() || versionResult.getData() == null) {
            throw new IllegalStateException("Failed to create draft version via article service");
        }
        CreateDraftResponse response = versionResult.getData();
        return DraftOptimizeResponse.builder()
                .draftId(response.getDraftId())
                .candidateVersionNo(response.getVersionNo())
                .title(optimized.getData().getTitle())
                .summary(optimized.getData().getSummary())
                .content(optimized.getData().getContent())
                .build();
    }

    @Override
    public ArticleSummaryResponse extractSummary(ExtractSummaryRequest request) {
        AiInvocationResult<ArticleSummaryResponse> summarized = articleAiGateway.extractSummary(
                request == null ? null : request.getTitle(),
                request == null ? null : request.getContent());
        if (summarized == null || summarized.getData() == null) {
            return ArticleSummaryResponse.builder().summary("").build();
        }
        return summarized.getData();
    }

    @Override
    public void adoptVersion(AdoptDraftVersionRequest request) {
        DraftVersionAdoptRequest adoptRequest = new DraftVersionAdoptRequest();
        adoptRequest.setDraftId(request.getDraftId());
        adoptRequest.setVersionNo(request.getVersionNo());
        articleClient.adoptDraftVersion(adoptRequest);
    }
}
