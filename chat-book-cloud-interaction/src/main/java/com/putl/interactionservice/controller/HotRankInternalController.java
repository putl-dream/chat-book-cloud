package com.putl.interactionservice.controller;

import com.putl.interactionservice.service.HotArticleRankService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "互动服务-热榜内部接口")
@RestController
@RequestMapping("/interaction/internal/hot")
@RequiredArgsConstructor
public class HotRankInternalController {

    private final HotArticleRankService hotArticleRankService;

    @Operation(summary = "总榜为空时初始化 Redis 热榜")
    @PostMapping("/initialize-all")
    public CommonResult<Long> initializeAllHotRankIfAbsent() {
        return CommonResult.success(hotArticleRankService.initializeAllRankIfAbsent());
    }
}
