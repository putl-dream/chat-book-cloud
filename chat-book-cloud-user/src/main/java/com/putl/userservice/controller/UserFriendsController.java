package com.putl.userservice.controller;

import com.putl.userservice.service.UserFriendsService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "用户好友关系")
@RestController
@RequestMapping("/user/friends")
@RequiredArgsConstructor
public class UserFriendsController {
    private final UserFriendsService userFriendsService;

    @Operation(summary = "新增关注")
    @GetMapping
    public CommonResult<String> addFriend(Integer userId, Integer friendId) {
        String userRelation = userFriendsService.addFriend(userId, friendId);
        return CommonResult.success(userRelation);
    }
}
