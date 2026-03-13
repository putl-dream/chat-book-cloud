package com.putl.userservice.api.fallback;

import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * UserClient fallback factory.
 *
 * @since 2026-01-31
 */
@Slf4j
@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("[UserClient] Call failed, fallback enabled.", cause);

        return new UserClient() {
            @Override
            public CommonResult<UserResult> getUserById(Integer id) {
                log.warn("[UserClient] getUserById fallback, userId: {}", id);
                return CommonResult.error(500, "User service unavailable");
            }
        };
    }
}
