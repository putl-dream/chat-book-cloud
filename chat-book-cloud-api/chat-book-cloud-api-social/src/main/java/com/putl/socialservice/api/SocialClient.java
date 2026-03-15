package com.putl.socialservice.api;

import com.putl.socialservice.api.config.SocialFeignConfig;
import com.putl.socialservice.api.dto.FollowStatDTO;
import com.putl.socialservice.api.dto.FriendRelationDTO;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "chat-book-cloud-social", configuration = SocialFeignConfig.class)
public interface SocialClient {

    @PostMapping("/social/follow/{followId}")
    CommonResult<String> follow(@PathVariable("followId") Integer followId);

    @DeleteMapping("/social/follow/{followId}")
    CommonResult<String> unfollow(@PathVariable("followId") Integer followId);

    @GetMapping("/social/relation/{targetUserId}")
    CommonResult<FriendRelationDTO> checkFriendRelation(@PathVariable("targetUserId") Integer targetUserId);

    @GetMapping("/social/follows")
    CommonResult<List<Integer>> getFollowList();

    @GetMapping("/social/fans")
    CommonResult<List<Integer>> getFanList();

    @GetMapping("/social/friends")
    CommonResult<List<Integer>> getFriendList();

    @GetMapping("/social/stat")
    CommonResult<FollowStatDTO> getFollowStat();

    @GetMapping("/social/stat/{userId}")
    CommonResult<FollowStatDTO> getUserFollowStat(@PathVariable("userId") Integer userId);
}
