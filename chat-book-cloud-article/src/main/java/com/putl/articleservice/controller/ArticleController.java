package com.putl.articleservice.controller;

import com.putl.articleservice.controller.vo.ArticleVO;
import com.putl.articleservice.service.ArticleService;
import com.putl.articleservice.utils.PageResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/article")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @PostMapping("/add")
    public void addArticle(@RequestBody ArticleVO articleVO) {
        articleService.addArticle(articleVO);
    }

    @PostMapping("/update")
    public void updateArticle(@RequestBody ArticleVO articleVO) {
        articleService.updateArticle(articleVO);
    }

    @PostMapping("/queryPage")
    public CommonResult<PageResult<ArticleVO>> queryPage(
        @RequestParam Integer pageNum, 
        @RequestParam Integer pageSize) {
        return CommonResult.success(articleService.queryPage(pageNum, pageSize));
    }

    @PostMapping("/query")
    public CommonResult<ArticleVO> getArticleInfo(Integer id) {
        return CommonResult.success(articleService.getArticleInfo(id));
    }

    @PostMapping("/delete")
    public void deleteArticle(Integer id) {
        articleService.deleteArticle(id);
    }
}
