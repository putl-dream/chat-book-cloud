package com.putl.socialservice.api.fallback;

import com.putl.socialservice.api.SocialClient;
import com.putl.userservice.api.vo.UserChatVO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SocialClientFallback implements FallbackFactory<SocialClient> {

    @Override
    public SocialClient create(Throwable cause) {
        log.error("[SocialClient] Call failed, fallback enabled.", cause);

        return new SocialClient() {
            @Override
            public CommonResult<String> follow(Integer followId) {
                log.warn("[SocialClient] follow fallback, followId: {}", followId);
                return CommonResult.error(500, "Social service unavailable");
            }

            @Override
            public CommonResult<String> unfollow(Integer followId) {
                log.warn("[SocialClient] unfollow fallback, followId: {}", followId);
                return CommonResult.error(500, "Social service unavailable");
            }

            @Override
            public CommonResult<List<Integer>> getFriendList() {
                log.warn("[SocialClient] getFriendList fallback");
                return CommonResult.error(500, "Social service unavailable");
            }

            @Override
            public CommonResult<List<UserChatVO>> getFriendListDetailed() {
                log.warn("[SocialClient] getFriendListDetailed fallback");
                return CommonResult.error(500, "Social service unavailable");
            }
        };
    }
}
