package com.putl.articleservice.controller;

import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import com.putl.articleservice.service.DraftService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "草稿服务")
@RestController
@RequestMapping("/draft")
@RequiredArgsConstructor
public class DraftController {

    private final DraftService draftService;

    @Operation(summary = "创建草稿")
    @PostMapping("/internal/create")
    public CommonResult<CreateDraftResponse> create(@RequestBody CreateDraftRequest request) {
        return CommonResult.success(draftService.createDraft(request));
    }

    @Operation(summary = "创建草稿版本")
    @PostMapping("/internal/version/create")
    public CommonResult<CreateDraftResponse> createVersion(@RequestBody CreateDraftVersionRequest request) {
        return CommonResult.success(draftService.createDraftVersion(request));
    }

    @Operation(summary = "获取草稿详情")
    @GetMapping("/internal/detail")
    public CommonResult<DraftDetailDTO> detail(@RequestParam("draftId") Integer draftId) {
        return CommonResult.success(draftService.getDraftDetail(draftId));
    }

    @Operation(summary = "采用草稿版本")
    @PostMapping("/internal/version/adopt")
    public CommonResult<Void> adopt(@RequestBody DraftVersionAdoptRequest request) {
        draftService.adoptVersion(request);
        return CommonResult.success();
    }
}
