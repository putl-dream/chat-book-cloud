package com.putl.interactionservice.controller;

import com.putl.interactionservice.service.ReviewService;
import com.putl.interactionservice.vo.ReviewListVO;
import com.putl.interactionservice.vo.ReviewVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "互动服务-评论接口")
@RestController
@RequestMapping("/interaction/review")
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
