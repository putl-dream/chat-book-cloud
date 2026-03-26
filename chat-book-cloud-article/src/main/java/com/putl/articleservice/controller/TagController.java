package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.TagPageRequestDTO;
import com.putl.articleservice.controller.vo.TagVO;
import com.putl.articleservice.service.TagService;
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

@Tag(name = "Tag API")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "Get tag page")
    @PostMapping("/page")
    public CommonResult<PageResult<TagVO>> getTagPage(@RequestBody TagPageRequestDTO request) {
        return CommonResult.success(tagService.getTagPage(request.getPageNo(), request.getPageSize(), request.getType()));
    }

    @Operation(summary = "Get all tags")
    @GetMapping("/list")
    public CommonResult<List<TagVO>> getAllTags() {
        return CommonResult.success(tagService.getAllTags());
    }

    @Operation(summary = "Get tags by type")
    @GetMapping("/listByType")
    public CommonResult<List<TagVO>> getTagsByType(@RequestParam Integer type) {
        return CommonResult.success(tagService.getTagsByType(type));
    }

    @Operation(summary = "Create tag")
    @PostMapping("/create")
    @RequireAdmin
    public CommonResult<TagVO> createTag(@RequestBody TagVO tagVO) {
        return CommonResult.success(tagService.createTag(tagVO));
    }

    @Operation(summary = "Update tag")
    @PostMapping("/update")
    @RequireAdmin
    public CommonResult<Void> updateTag(@RequestBody TagVO tagVO) {
        tagService.updateTag(tagVO);
        return CommonResult.success(null);
    }

    @Operation(summary = "Delete tag")
    @DeleteMapping("/delete")
    @RequireAdmin
    public CommonResult<Void> deleteTag(@RequestParam Integer tagId) {
        tagService.deleteTag(tagId);
        return CommonResult.success(null);
    }
}
