package com.putl.articleservice.api;

import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient("chat-book-cloud-article")
public interface ArticleClient {

    String USER_SERVICE_URL = "/article/";

    @GetMapping(USER_SERVICE_URL + "query")
    CommonResult<ArticleVO> queryArticle(@RequestParam("id") Integer id);

    @PostMapping("page/ids")
    CommonResult<List<ArticleListVO>> selectIds(@RequestBody List<Integer> ids);

    @PostMapping(USER_SERVICE_URL + "queryCount")
    Long queryCount();
}
