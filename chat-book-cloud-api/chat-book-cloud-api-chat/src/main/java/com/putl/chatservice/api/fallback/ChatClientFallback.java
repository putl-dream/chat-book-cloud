package com.putl.chatservice.api.fallback;

import com.putl.chatservice.api.ChatClient;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ChatClientFallback implements FallbackFactory<ChatClient> {

    @Override
    public ChatClient create(Throwable cause) {
        log.error("[ChatClient] Call failed, fallback enabled.", cause);

        return new ChatClient() {
            @Override
            public CommonResult<List<Map<String, Object>>> getHistoryMessages(Integer targetUserId, Integer page, Integer size) {
                log.warn("[ChatClient] getHistoryMessages fallback, targetUserId: {}, page: {}, size: {}", targetUserId, page, size);
                return CommonResult.error(500, "Chat service unavailable");
            }

            @Override
            public CommonResult<Integer> getUnreadCount() {
                log.warn("[ChatClient] getUnreadCount fallback");
                return CommonResult.error(500, "Chat service unavailable");
            }

            @Override
            public CommonResult<Void> markAsRead(Integer targetUserId) {
                log.warn("[ChatClient] markAsRead fallback, targetUserId: {}", targetUserId);
                return CommonResult.error(500, "Chat service unavailable");
            }

            @Override
            public CommonResult<Map<String, Object>> getLastMessage(Integer userId, Integer targetUserId) {
                log.warn("[ChatClient] getLastMessage fallback, userId: {}, targetUserId: {}", userId, targetUserId);
                return CommonResult.error(500, "Chat service unavailable");
            }
        };
    }
}
