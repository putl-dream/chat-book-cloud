package com.putl.interactionservice.api;

import com.putl.interactionservice.api.config.InteractionFeignConfig;
import com.putl.interactionservice.api.dto.NotificationVO;
import com.putl.interactionservice.api.dto.ReviewListVO;
import com.putl.interactionservice.api.dto.ReviewVO;
import com.putl.interactionservice.api.dto.UserFootListVO;
import com.putl.interactionservice.api.dto.UserFootVO;
import com.putl.articleservice.api.dto.ArticleListVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "interaction-service", configuration = InteractionFeignConfig.class)
public interface InteractionClient {

    @PostMapping("/interaction/foot/browse")
    boolean addBrowse(@RequestParam("articleId") Integer articleId, @RequestParam("userId") Integer userId);

    @PostMapping("/interaction/foot/praise")
    CommonResult<Integer> updatePraise(@RequestParam("articleId") Integer articleId);

    @PostMapping("/interaction/foot/collection")
    CommonResult<Integer> updateCollection(@RequestParam("articleId") Integer articleId);

    @PostMapping("/interaction/foot/comment")
    CommonResult<Integer> updateComment(@RequestParam("articleId") Integer articleId);

    @GetMapping("/interaction/foot/getUserFoot")
    UserFootVO getUserFoot(@RequestParam("articleId") Integer articleId, @RequestParam("userId") Integer userId);

    @GetMapping("/interaction/foot/getUserFootList")
    UserFootListVO getUserFootList(@RequestParam("articleId") Integer articleId);

    @GetMapping("/interaction/foot/getHistory")
    CommonResult<List<ArticleListVO>> getHistory(@RequestParam("page") Integer page, @RequestParam("size") Integer size);

    @GetMapping("/interaction/foot/getNotifications")
    CommonResult<List<NotificationVO>> getNotifications();

    @GetMapping("/interaction/review/getByArticleId")
    CommonResult<List<ReviewListVO>> getReviewsByArticleId(@RequestParam("articleId") Integer articleId);

    @PostMapping("/interaction/review/save")
    CommonResult<String> saveReview(@RequestBody ReviewVO reviewVO);
}
