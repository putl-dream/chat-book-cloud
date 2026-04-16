package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.ArticleSystemTagUpdateRequestDTO;
import com.putl.articleservice.controller.dto.SystemTagPageRequestDTO;
import com.putl.articleservice.controller.dto.SystemTagSaveRequestDTO;
import com.putl.articleservice.controller.vo.SystemTagVO;
import com.putl.articleservice.service.SystemTagService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system-tag")
@RequiredArgsConstructor
@Tag(name = "系统标签接口")
public class SystemTagController {

    private final SystemTagService systemTagService;

    @Operation(summary = "系统标签分页")
    @PostMapping("/page")
    @RequireAdmin
    public CommonResult<PageResult<SystemTagVO>> page(@RequestBody SystemTagPageRequestDTO request) {
        return CommonResult.success(systemTagService.getPage(
                request.getPageNo(),
                request.getPageSize(),
                request.getKeyword(),
                request.getDimension(),
                request.getStatus()
        ));
    }

    @Operation(summary = "获取全部启用的系统标签")
    @GetMapping("/list")
    @RequireAdmin
    public CommonResult<List<SystemTagVO>> list() {
        return CommonResult.success(systemTagService.getAllActiveTags());
    }

    @Operation(summary = "创建系统标签")
    @PostMapping("/create")
    @RequireAdmin
    public CommonResult<SystemTagVO> create(@RequestBody SystemTagSaveRequestDTO request) {
        return CommonResult.success(systemTagService.create(request));
    }

    @Operation(summary = "更新系统标签")
    @PostMapping("/update")
    @RequireAdmin
    public CommonResult<Void> update(@RequestBody SystemTagSaveRequestDTO request) {
        systemTagService.update(request);
        return CommonResult.success(null);
    }

    @Operation(summary = "删除系统标签")
    @DeleteMapping("/delete")
    @RequireAdmin
    public CommonResult<Void> delete(@RequestParam Integer systemTagId) {
        systemTagService.delete(systemTagId);
        return CommonResult.success(null);
    }

    @Operation(summary = "后台人工修正文章系统标签")
    @PostMapping("/article/update")
    @RequireAdmin
    public CommonResult<Void> updateArticleSystemTags(@RequestBody ArticleSystemTagUpdateRequestDTO request) {
        systemTagService.updateArticleSystemTagsByAdmin(request.getArticleId(), request.getSystemTagIds());
        return CommonResult.success(null);
    }
}
