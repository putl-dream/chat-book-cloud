package com.putl.chatservice.controller;

import com.putl.chatservice.service.MessageService;
import com.putl.chatservice.vo.MessageVO;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天 Controller
 */
@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "聊天管理", description = "消息发送、历史查询、未读数统计")
public class ChatController {

    private final MessageService messageService;

    @GetMapping("/messages")
    @Operation(summary = "获取历史消息")
    public CommonResult<List<MessageVO>> getHistoryMessages(
            @RequestParam Integer targetUserId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "50") Integer size) {
        String userIdStr = UserContext.getUserId();
        Integer userId = userIdStr != null ? Integer.parseInt(userIdStr) : null;
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }
        List<MessageVO> messages = messageService.getHistoryMessages(userId, targetUserId, page, size);
        return CommonResult.success(messages);
    }

    @GetMapping("/unread/count")
    @Operation(summary = "获取未读消息数")
    public CommonResult<Integer> getUnreadCount() {
        String userIdStr = UserContext.getUserId();
        Integer userId = userIdStr != null ? Integer.parseInt(userIdStr) : null;
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }
        Integer count = messageService.getUnreadCount(userId);
        return CommonResult.success(count);
    }

    @PutMapping("/messages/read")
    @Operation(summary = "标记消息已读")
    public CommonResult<Void> markAsRead(@RequestParam Integer targetUserId) {
        String userIdStr = UserContext.getUserId();
        Integer userId = userIdStr != null ? Integer.parseInt(userIdStr) : null;
        if (userId == null) {
            return CommonResult.error(401, "用户未登录");
        }
        messageService.markAsRead(userId, targetUserId);
        return CommonResult.success(null);
    }
}
