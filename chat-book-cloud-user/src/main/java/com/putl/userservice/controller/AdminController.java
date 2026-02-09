package com.putl.userservice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.putl.userservice.client.ArticleClient;
import com.putl.userservice.controller.vo.DataCount;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.service.MessageService;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "管理员服务")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/admin")
public class AdminController {
    private final MessageService messageService;
    private final UserService userService;
    private final ArticleClient articleClient;

    @GetMapping("/count")
    public CommonResult<DataCount> getDataCount() {
        DataCount dataCount = new DataCount();
        dataCount.setUserCount(userService.count());
        dataCount.setArticleCount(articleClient.queryCount());
        dataCount.setReviewCount(messageService.count());
        return CommonResult.success(dataCount);
    }

    @GetMapping("/user")
    public CommonResult<IPage<UserVO>> getUserPage(Integer page, Integer size) {
        return CommonResult.success(userService.selectPage(page, size));
    }
}
