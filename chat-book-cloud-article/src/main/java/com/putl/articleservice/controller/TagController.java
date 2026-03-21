package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.TagPageRequestDTO;
import com.putl.articleservice.controller.vo.TagVO;
import com.putl.articleservice.service.TagService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 标签管理控制器
 */
@Tag(name = "标签管理")
@RestController
@RequestMapping("/tag")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "分页查询标签")
    @PostMapping("/page")
    public CommonResult<PageResult<TagVO>> getTagPage(@RequestBody TagPageRequestDTO request) {
        return CommonResult.success(tagService.getTagPage(request.getPageNo(), request.getPageSize(), request.getType()));
    }

    @Operation(summary = "获取所有标签")
    @GetMapping("/list")
    public CommonResult<List<TagVO>> getAllTags() {
        return CommonResult.success(tagService.getAllTags());
    }

    @Operation(summary = "根据类型获取标签")
    @GetMapping("/listByType")
    public CommonResult<List<TagVO>> getTagsByType(@RequestParam Integer type) {
        return CommonResult.success(tagService.getTagsByType(type));
    }

    @Operation(summary = "创建标签")
    @PostMapping("/create")
    public CommonResult<TagVO> createTag(@RequestBody TagVO tagVO) {
        return CommonResult.success(tagService.createTag(tagVO));
    }

    @Operation(summary = "更新标签")
    @PostMapping("/update")
    public CommonResult<Void> updateTag(@RequestBody TagVO tagVO) {
        tagService.updateTag(tagVO);
        return CommonResult.success(null);
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/delete")
    public CommonResult<Void> deleteTag(@RequestParam Integer tagId) {
        tagService.deleteTag(tagId);
        return CommonResult.success(null);
    }
}
