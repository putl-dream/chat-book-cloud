package com.putl.userservice.api;

import com.putl.userservice.api.dto.UserResult;
import com.putl.userservice.api.fallback.UserClientFallback;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "user-service", fallbackFactory = UserClientFallback.class)
public interface UserClient {

    String USER_SERVICE_URL = "user/";

    @GetMapping(USER_SERVICE_URL + "byId")
    CommonResult<UserResult> getUserById(@RequestParam Integer id);
}
