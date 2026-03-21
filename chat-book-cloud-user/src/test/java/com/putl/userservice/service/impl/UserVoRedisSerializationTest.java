package com.putl.userservice.service.impl;

import com.putl.userservice.controller.vo.UserVO;
import fun.amireux.chat.book.framework.redis.config.RedisConfig;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserVoRedisSerializationTest {

    @Test
    void shouldDeserializeCachedUserVoList() throws Exception {
        GenericJackson2JsonRedisSerializer serializer = createRedisSerializer();

        List<UserVO> source = new ArrayList<>();
        source.add(UserVO.builder()
                .id(1)
                .userId(1)
                .username("Init")
                .email("rule0000@qq.com")
                .photo("https://haowallpaper.com/link/common/file/getCroppingImg/15539078305452352")
                .profile("这个人很懒，什么也没写")
                .role("user")
                .build());

        Object restored = serializer.deserialize(serializer.serialize(source));

        assertThat(restored).isInstanceOf(List.class);
        assertThat((List<?>) restored).hasSize(1);
        assertThat(((List<?>) restored).get(0)).isInstanceOf(UserVO.class);
        UserVO user = (UserVO) ((List<?>) restored).get(0);
        assertThat(user.getUserId()).isEqualTo(1);
        assertThat(user.getUsername()).isEqualTo("Init");
    }

    private GenericJackson2JsonRedisSerializer createRedisSerializer() throws Exception {
        RedisConfig config = new RedisConfig();
        var method = RedisConfig.class.getDeclaredMethod("createRedisSerializer");
        method.setAccessible(true);
        return (GenericJackson2JsonRedisSerializer) method.invoke(config);
    }
}
