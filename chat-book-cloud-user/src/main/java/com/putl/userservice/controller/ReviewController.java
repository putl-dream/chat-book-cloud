package com.putl.userservice.controller;

import com.putl.userservice.controller.vo.ReviewListVO;
import com.putl.userservice.controller.vo.ReviewVO;
import com.putl.userservice.service.ReviewService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


/**
 * [Review]表服务接口  评论表
 *
 * @since 2025-01-18 12:03:24
 */

@Tag(name = "评论")
@RestController
@RequestMapping("/user/review")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;

    @Operation(summary = "查询文章评论")
    @GetMapping("/getByArticleId")
    public CommonResult<List<ReviewListVO>> getByArticleId(@RequestParam Integer articleId) {
        return CommonResult.success(reviewService.getByArticleId(articleId));
    }

    @Operation(summary = "添加评论")
    @PostMapping("/save")
    public CommonResult<String> save(@RequestBody ReviewVO reviewVO) {
        boolean save = reviewService.save(reviewVO);
        return save ? CommonResult.success("评论成功") : CommonResult.error(500, "评论失败");
    }

}

