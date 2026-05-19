package com.putl.articleservice.api;

import com.putl.articleservice.api.dto.ArticleListVO;
import com.putl.articleservice.api.dto.ArticleVO;
import com.putl.articleservice.api.dto.CreateDraftRequest;
import com.putl.articleservice.api.dto.CreateDraftResponse;
import com.putl.articleservice.api.dto.CreateDraftVersionRequest;
import com.putl.articleservice.api.dto.DraftDetailDTO;
import com.putl.articleservice.api.dto.DraftVersionAdoptRequest;
import com.putl.articleservice.api.fallback.ArticleClientFallback;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 文章服务Feign客户端
 * <p>提供对文章微服务的远程调用接口，包括文章查询、草稿管理等功能</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
@FeignClient(name = "chat-book-cloud-article", fallbackFactory = ArticleClientFallback.class)
public interface ArticleClient {

    /**
     * 文章服务基础URL路径
     */
    String USER_SERVICE_URL = "/article/";

    /**
     * 查询文章详情
     *
     * @param id 文章ID
     * @return 文章详细信息
     */
    @GetMapping(USER_SERVICE_URL + "query")
    CommonResult<ArticleVO> queryArticle(@RequestParam("id") Integer id);

    /**
     * 批量查询文章列表
     *
     * @param ids 文章ID列表
     * @return 文章列表信息
     */
    @PostMapping("page/ids")
    CommonResult<List<ArticleListVO>> selectIds(@RequestBody List<Integer> ids);

    /**
     * 查询文章总数
     *
     * @return 文章总数量
     */
    @GetMapping(USER_SERVICE_URL + "queryCount")
    CommonResult<Long> queryCount();

    /**
     * 查询待审核文章数量
     *
     * @return 待审核文章数量
     */
    @GetMapping(USER_SERVICE_URL + "queryPendingReviewCount")
    CommonResult<Long> queryPendingReviewCount();

    /**
     * 创建文章草稿
     *
     * @param request 创建草稿请求参数
     * @return 创建结果，包含草稿ID等信息
     */
    @PostMapping("/draft/internal/create")
    CommonResult<CreateDraftResponse> createDraft(@RequestBody CreateDraftRequest request);

    /**
     * 创建草稿版本
     *
     * @param request 创建草稿版本请求参数
     * @return 创建结果，包含版本信息
     */
    @PostMapping("/draft/internal/version/create")
    CommonResult<CreateDraftResponse> createDraftVersion(@RequestBody CreateDraftVersionRequest request);

    /**
     * 获取草稿详情
     *
     * @param draftId 草稿ID
     * @return 草稿详细信息
     */
    @GetMapping("/draft/internal/detail")
    CommonResult<DraftDetailDTO> getDraftDetail(@RequestParam("draftId") Integer draftId);

    /**
     * 采用草稿版本
     *
     * @param request 采用草稿版本请求参数
     * @return 操作结果
     */
    @PostMapping("/draft/internal/version/adopt")
    CommonResult<Void> adoptDraftVersion(@RequestBody DraftVersionAdoptRequest request);

    /**
     * 清除热门页面缓存
     *
     * @return 操作结果
     */
    @PostMapping("/cache/internal/evict/hot")
    CommonResult<Void> evictHotPageCache();
}
