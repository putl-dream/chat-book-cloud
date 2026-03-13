package com.putl.interactionservice.api;

import com.putl.interactionservice.api.config.InteractionFeignConfig;
import com.putl.interactionservice.api.dto.UserFootListVO;
import com.putl.interactionservice.api.dto.UserFootVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "interaction-service", configuration = InteractionFeignConfig.class)
public interface InteractionClient {

    @PostMapping("/interaction/foot/browse")
    CommonResult<Boolean> addBrowse(@RequestParam("articleId") Integer articleId, @RequestParam("userId") Integer userId);

    @PostMapping("/interaction/foot/praise")
    CommonResult<Integer> updatePraise(@RequestParam("articleId") Integer articleId);

    @PostMapping("/interaction/foot/collection")
    CommonResult<Integer> updateCollection(@RequestParam("articleId") Integer articleId);

    @GetMapping("/interaction/foot/getUserFoot")
    CommonResult<UserFootVO> getUserFoot(@RequestParam("articleId") Integer articleId, @RequestParam("userId") Integer userId);

    @GetMapping("/interaction/foot/getUserFootList")
    CommonResult<UserFootListVO> getUserFootList(@RequestParam("articleId") Integer articleId);
}
