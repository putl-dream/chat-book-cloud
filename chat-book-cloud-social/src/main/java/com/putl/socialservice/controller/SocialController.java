package com.putl.socialservice.controller;

import com.putl.socialservice.enums.FriendRelationEnum;
import com.putl.socialservice.service.UserFollowService;
import com.putl.socialservice.vo.FollowStatVO;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社交关系控制器
 */
@Tag(name = "社交关系")
@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final UserFollowService userFollowService;

    @Operation(summary = "关注用户")
    @PostMapping("/follow/{followId}")
    public CommonResult<String> follow(
            @Parameter(description = "被关注者用户ID") @PathVariable Integer followId) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        String result = userFollowService.follow(userId, followId);
        return CommonResult.success(result);
    }

    @Operation(summary = "取消关注")
    @DeleteMapping("/follow/{followId}")
    public CommonResult<String> unfollow(
            @Parameter(description = "被关注者用户ID") @PathVariable Integer followId) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        String result = userFollowService.unfollow(userId, followId);
        return CommonResult.success(result);
    }

    @Operation(summary = "检查好友关系")
    @GetMapping("/relation/{targetUserId}")
    public CommonResult<Integer> checkFriendRelation(
            @Parameter(description = "目标用户ID") @PathVariable Integer targetUserId) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        FriendRelationEnum relation = userFollowService.checkFriendRelation(userId, targetUserId);
        return CommonResult.success(relation.getCode());
    }

    @Operation(summary = "获取关注列表")
    @GetMapping("/follows")
    public CommonResult<List<Integer>> getFollowList() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        List<Integer> list = userFollowService.getFollowList(userId);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取粉丝列表")
    @GetMapping("/fans")
    public CommonResult<List<Integer>> getFanList() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        List<Integer> list = userFollowService.getFanList(userId);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取好友列表")
    @GetMapping("/friends")
    public CommonResult<List<Integer>> getFriendList() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        List<Integer> list = userFollowService.getFriendList(userId);
        return CommonResult.success(list);
    }

    @Operation(summary = "获取关注统计")
    @GetMapping("/stat")
    public CommonResult<FollowStatVO> getFollowStat() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        FollowStatVO stat = userFollowService.getFollowStat(userId);
        return CommonResult.success(stat);
    }

    @Operation(summary = "获取指定用户的关注统计")
    @GetMapping("/stat/{userId}")
    public CommonResult<FollowStatVO> getUserFollowStat(
            @Parameter(description = "用户ID") @PathVariable Integer userId) {
        FollowStatVO stat = userFollowService.getFollowStat(userId);
        return CommonResult.success(stat);
    }
}
