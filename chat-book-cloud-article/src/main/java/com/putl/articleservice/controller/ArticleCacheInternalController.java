package com.putl.articleservice.controller;

import com.putl.articleservice.service.ArticleListCacheService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "鏂囩珷缂撳瓨鍐呴儴鎺ュ彛")
@RestController
@RequestMapping("/cache/internal")
@RequiredArgsConstructor
public class ArticleCacheInternalController {

    private final ArticleListCacheService articleListCacheService;

    @Operation(summary = "娓呯悊鐑缂撳瓨")
    @PostMapping("/evict/hot")
    public CommonResult<Void> evictHotPageCache() {
        articleListCacheService.evictHotCache();
        articleListCacheService.evictTodayHotCache();
        return CommonResult.success();
    }
}
