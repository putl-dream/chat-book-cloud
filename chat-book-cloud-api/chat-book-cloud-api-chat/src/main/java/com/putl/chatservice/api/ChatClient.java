package com.putl.chatservice.api;

import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Map;

/**
 * Chat Service Feign Client
 */
@FeignClient(name = "chat-book-cloud-chat", path = "/chat")
public interface ChatClient {

    /**
     * 获取历史消息
     */
    @GetMapping("/messages")
    CommonResult<List<Map<String, Object>>> getHistoryMessages(
            @RequestParam("targetUserId") Integer targetUserId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "50") Integer size);

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread/count")
    CommonResult<Integer> getUnreadCount();

    /**
     * 标记消息已读
     */
    @PutMapping("/messages/read")
    CommonResult<Void> markAsRead(@RequestParam("targetUserId") Integer targetUserId);

    /**
     * 查询最后一条消息
     */
    @GetMapping("/messages/last")
    CommonResult<Map<String, Object>> getLastMessage(@RequestParam("userId") Integer userId, @RequestParam("targetUserId") Integer targetUserId);
}
