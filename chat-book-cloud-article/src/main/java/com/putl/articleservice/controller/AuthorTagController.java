package com.putl.articleservice.controller;

import com.putl.articleservice.controller.dto.AuthorTagPageRequestDTO;
import com.putl.articleservice.controller.vo.AuthorTagVO;
import com.putl.articleservice.service.AuthorTagService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/author-tag")
@RequiredArgsConstructor
@Tag(name = "作者标签接口")
public class AuthorTagController {

    private final AuthorTagService authorTagService;

    @Operation(summary = "搜索作者标签")
    @GetMapping("/search")
    public CommonResult<List<AuthorTagVO>> search(@RequestParam(required = false) String keyword,
                                                  @RequestParam(required = false) Integer limit) {
        return CommonResult.success(authorTagService.search(keyword, limit));
    }

    @Operation(summary = "热门作者标签")
    @GetMapping("/hot")
    public CommonResult<List<AuthorTagVO>> hot(@RequestParam(required = false) Integer limit) {
        return CommonResult.success(authorTagService.getHotTags(limit));
    }

    @Operation(summary = "后台作者标签分页")
    @PostMapping("/admin/page")
    @RequireAdmin
    public CommonResult<PageResult<AuthorTagVO>> adminPage(@RequestBody AuthorTagPageRequestDTO request) {
        return CommonResult.success(authorTagService.getAdminPage(request.getPageNo(), request.getPageSize(), request.getKeyword()));
    }
}
