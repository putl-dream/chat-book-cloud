package com.putl.interactionservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.putl.interactionservice.service.ReviewService;
import com.putl.interactionservice.vo.ReviewVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 后台评论治理接口
 */
@Tag(name = "后台评论治理接口")
@RestController
@RequestMapping("/interaction/admin/review")
@RequiredArgsConstructor
@RequireAdmin
public class ReviewAdminController {

    private final ReviewService reviewService;

    @Operation(summary = "评论分页查询（管理员）")
    @GetMapping("/page")
    public CommonResult<IPage<ReviewVO>> getAdminPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Integer articleId,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return CommonResult.success(reviewService.getAdminPage(
                page, size, articleId, userId, keyword, status, startTime, endTime));
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/{id}")
    public CommonResult<Void> delete(@PathVariable Integer id) {
        reviewService.deleteReview(id);
        return CommonResult.success();
    }

    @Operation(summary = "屏蔽评论")
    @PutMapping("/{id}/hide")
    public CommonResult<Void> hide(@PathVariable Integer id) {
        reviewService.hideReview(id);
        return CommonResult.success();
    }

    @Operation(summary = "恢复评论")
    @PutMapping("/{id}/restore")
    public CommonResult<Void> restore(@PathVariable Integer id) {
        reviewService.restoreReview(id);
        return CommonResult.success();
    }
}
