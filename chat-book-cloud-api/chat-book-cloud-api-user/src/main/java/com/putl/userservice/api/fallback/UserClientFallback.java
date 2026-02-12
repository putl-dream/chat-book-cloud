package com.putl.userservice.api.fallback;

import com.putl.userservice.api.UserClient;
import com.putl.userservice.api.dto.UserFootListVO;
import com.putl.userservice.api.dto.UserFootVO;
import com.putl.userservice.api.dto.UserResult;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

/**
 * UserClient 降级工厂
 * 当用户服务不可用时，提供默认返回值，避免服务雪崩
 *
 * @since 2026-01-31
 */
@Slf4j
@Component
public class UserClientFallback implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("[UserClient] 调用用户服务失败，启用降级处理", cause);

        return new UserClient() {

            @Override
            public CommonResult<UserResult> getUserById(Integer id) {
                log.warn("[UserClient] getUserById 降级处理，userId: {}", id);
                return CommonResult.error(500, "用户服务暂时不可用");
            }

            @Override
            public UserFootVO getUserFoot(Integer articleId, Integer userId) {
                log.warn("[UserClient] getUserFoot 降级处理，articleId: {}, userId: {}", articleId, userId);
                return getDefaultUserFootVO(articleId);
            }

            @Override
            public UserFootVO getUserFoot(Integer articleId) {
                log.warn("[UserClient] getUserFoot 降级处理，articleId: {}", articleId);
                return getDefaultUserFootVO(articleId);
            }

            @Override
            public UserFootListVO getUserFootList(Integer articleId) {
                log.warn("[UserClient] getUserFootList 降级处理，articleId: {}", articleId);
                UserFootListVO defaultList = new UserFootListVO();
                defaultList.setArticleId(articleId);
                defaultList.setPraiseCount(0L);
                defaultList.setCollectCount(0L);
                defaultList.setViewCount(0L);
                return defaultList;
            }

            @Override
            public boolean addBrowse(Integer articleId, Integer userId) {
                log.warn("[UserClient] addBrowse 降级处理，articleId: {}, userId: {}", articleId, userId);
                return false;
            }

            /**
             * 创建默认的 UserFootVO
             */
            private UserFootVO getDefaultUserFootVO(Integer articleId) {
                return UserFootVO.builder()
                        .articleId(articleId)
                        .userId(0)
                        .praiseStat(0)
                        .collectStat(0)
                        .viewCount(0L)
                        .build();
            }
        };
    }
}
