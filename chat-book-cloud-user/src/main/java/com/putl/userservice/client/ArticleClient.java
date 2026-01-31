package com.putl.userservice.client;


import com.putl.userservice.client.result.ArticleListVO;
import com.putl.userservice.client.result.ArticleVO;
import com.putl.userservice.util.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;


import java.util.List;

@FeignClient("article-service")
public interface ArticleClient {

    String USER_SERVICE_URL = "article/";

    @PostMapping(USER_SERVICE_URL + "queryOpen")
    Result<ArticleVO> queryArticle(@RequestParam Integer id);

    @PostMapping(USER_SERVICE_URL + "page/ids")
    Result<List<ArticleListVO>> selectIds(@RequestBody List<Integer> ids);

    @PostMapping(USER_SERVICE_URL + "queryCount")
    Long queryCount();
}
