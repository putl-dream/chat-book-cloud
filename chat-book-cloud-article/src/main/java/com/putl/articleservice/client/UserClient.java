package com.putl.articleservice.client;

import com.putl.articleservice.client.result.UserFootListVO;
import com.putl.articleservice.client.result.UserFootVO;
import com.putl.articleservice.client.result.UserResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallbackFactory = com.putl.articleservice.client.impl.UserClientFallback.class)
public interface UserClient {

    String USER_SERVICE_URL = "user/";

    @GetMapping(USER_SERVICE_URL + "byId")
    CommonResult<UserResult> getUserById(@RequestParam Integer id);

    @GetMapping(USER_SERVICE_URL + "foot/getUserFoot")
    UserFootVO getUserFoot(@RequestParam Integer articleId, @RequestParam Integer userId);


    @GetMapping(USER_SERVICE_URL + "foot/article")
    UserFootVO getUserFoot(@RequestParam Integer articleId);

    @GetMapping(USER_SERVICE_URL + "foot/getUserFootList")
    UserFootListVO getUserFootList(@RequestParam Integer articleId);

    @GetMapping(USER_SERVICE_URL + "foot/browse")
    boolean addBrowse(@RequestParam Integer articleId, @RequestParam Integer userId);


}
