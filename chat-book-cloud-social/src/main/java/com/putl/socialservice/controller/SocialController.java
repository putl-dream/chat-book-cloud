package com.putl.socialservice.controller;

import com.putl.socialservice.service.UserFollowService;
import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserResult;
import com.putl.userservice.api.vo.UserChatVO;
import com.putl.chatservice.api.ChatClient;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 社交关系控制器
 */
@Tag(name = "社交关系")
@RestController
@RequestMapping("/social")
@RequiredArgsConstructor
public class SocialController {

    private final UserFollowService userFollowService;
    private final UserClient userClient;
    private final ChatClient chatClient;

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

    @Operation(summary = "获取详细好友列表")
    @GetMapping("/friends/detailed")
    public CommonResult<List<UserChatVO>> getFriendListDetailed() {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer userId = Integer.parseInt(userIdStr);
        List<Integer> friendIds = userFollowService.getFriendList(userId);
        
        List<UserChatVO> detailedList = friendIds.stream().map(friendId -> {
            CommonResult<UserResult> userResult = userClient.getUserById(friendId);
            if (userResult == null || userResult.getData() == null) {
                return null;
            }
            UserResult user = userResult.getData();
            
            CommonResult<Map<String, Object>> lastMsgResult = chatClient.getLastMessage(userId, friendId);
            String lastChat = "";
            LocalDateTime lastTime = null;
            if (lastMsgResult != null && lastMsgResult.getData() != null) {
                Map<String, Object> lastMsg = lastMsgResult.getData();
                lastChat = (String) lastMsg.get("content");
                Object sentTimeObj = lastMsg.get("sentTime");
                if (sentTimeObj instanceof String) {
                   lastTime = LocalDateTime.parse((String)sentTimeObj);
                }
            }
            
            return UserChatVO.builder()
                    .userId(friendId)
                    .username(user.getUsername())
                    .photo(user.getPhoto())
                    .profile(user.getProfile())
                    .lastChat(lastChat)
                    .lastTime(lastTime)
                    .build();
        }).filter(Objects::nonNull).toList();
        
        return CommonResult.success(detailedList);
    }
}
