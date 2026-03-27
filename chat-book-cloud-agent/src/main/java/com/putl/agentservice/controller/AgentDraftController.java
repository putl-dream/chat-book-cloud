package com.putl.agentservice.controller;

import com.putl.agentservice.model.dto.AdoptDraftVersionRequest;
import com.putl.agentservice.model.dto.GenerateDraftRequest;
import com.putl.agentservice.model.dto.OptimizeDraftRequest;
import com.putl.agentservice.model.vo.DraftGenerateResponse;
import com.putl.agentservice.model.vo.DraftOptimizeResponse;
import com.putl.agentservice.service.DraftGenerationService;
import com.putl.agentservice.service.DraftOptimizationService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent 草稿")
@RestController
@RequestMapping("/agent/draft")
public class AgentDraftController {

    private final DraftGenerationService draftGenerationService;
    private final DraftOptimizationService draftOptimizationService;

    public AgentDraftController(DraftGenerationService draftGenerationService,
                                DraftOptimizationService draftOptimizationService) {
        this.draftGenerationService = draftGenerationService;
        this.draftOptimizationService = draftOptimizationService;
    }

    @Operation(summary = "从会话生成草稿")
    @PostMapping("/generate")
    public CommonResult<DraftGenerateResponse> generate(@RequestBody GenerateDraftRequest request) {
        return CommonResult.success(draftGenerationService.generateDraft(request));
    }

    @Operation(summary = "发起草稿优化")
    @PostMapping("/optimize")
    public CommonResult<DraftOptimizeResponse> optimize(@RequestBody OptimizeDraftRequest request) {
        return CommonResult.success(draftOptimizationService.optimizeDraft(request));
    }

    @Operation(summary = "采用某个草稿版本")
    @PostMapping("/version/adopt")
    public CommonResult<Void> adopt(@RequestBody AdoptDraftVersionRequest request) {
        draftOptimizationService.adoptVersion(request);
        return CommonResult.success();
    }
}
