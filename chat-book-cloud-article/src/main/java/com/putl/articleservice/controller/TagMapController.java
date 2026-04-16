package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.TagMapBatchApplyRequestDTO;
import com.putl.articleservice.controller.dto.TagMapPageRequestDTO;
import com.putl.articleservice.controller.dto.TagMapUpdateRequestDTO;
import com.putl.articleservice.controller.vo.TagMapVO;
import com.putl.articleservice.service.TagMapService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tag-map")
@RequiredArgsConstructor
@RequireAdmin
@Tag(name = "标签映射接口")
public class TagMapController {

    private final TagMapService tagMapService;

    @Operation(summary = "映射分页")
    @PostMapping("/page")
    public CommonResult<PageResult<TagMapVO>> page(@RequestBody TagMapPageRequestDTO request) {
        return CommonResult.success(tagMapService.getPage(
                request.getPageNo(),
                request.getPageSize(),
                request.getKeyword(),
                request.getMappedOnly()
        ));
    }

    @Operation(summary = "更新单个作者标签映射")
    @PostMapping("/update")
    public CommonResult<Void> update(@RequestBody TagMapUpdateRequestDTO request) {
        tagMapService.updateMappings(request.getAuthorTagId(), request.getSystemTagIds());
        return CommonResult.success(null);
    }

    @Operation(summary = "按映射批量回刷文章系统标签")
    @PostMapping("/batch-apply")
    public CommonResult<Void> batchApply(@RequestBody TagMapBatchApplyRequestDTO request) {
        tagMapService.batchApply(request.getAuthorTagIds());
        return CommonResult.success(null);
    }
}
