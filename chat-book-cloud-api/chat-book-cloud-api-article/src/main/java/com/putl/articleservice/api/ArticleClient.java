package com.putl.articleservice.api;

import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
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

    @GetMapping(USER_SERVICE_URL + "queryCount")
    CommonResult<Long> queryCount();

    @GetMapping(USER_SERVICE_URL + "queryPendingReviewCount")
    CommonResult<Long> queryPendingReviewCount();

    @PostMapping("/draft/internal/create")
    CommonResult<CreateDraftResponse> createDraft(@RequestBody CreateDraftRequest request);

    @PostMapping("/draft/internal/version/create")
    CommonResult<CreateDraftResponse> createDraftVersion(@RequestBody CreateDraftVersionRequest request);

    @GetMapping("/draft/internal/detail")
    CommonResult<DraftDetailDTO> getDraftDetail(@RequestParam("draftId") Integer draftId);

    @PostMapping("/draft/internal/version/adopt")
    CommonResult<Void> adoptDraftVersion(@RequestBody DraftVersionAdoptRequest request);
}
