package com.putl.userservice.controller;

import fun.amireux.chat.book.framework.common.context.UserContext;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "用户服务")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @Operation(summary = "根据id查询用户")
    @GetMapping("/byId")
    public CommonResult<UserVO> getUserById(@RequestParam Integer id){
        UserVO user = userService.selectById(id);
        return CommonResult.success(user);
    }

    @Operation(summary = "查询自己")
    @GetMapping("/bySelf")
    public CommonResult<UserVO> getUserBySelf(){
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        UserVO user = userService.selectById(Integer.parseInt(userId));
        return CommonResult.success(user);
    }

    @Operation(summary = "根据id查询用户好友列表")
    @GetMapping("/friendList")
    public CommonResult<List<UserChatVO>> getFriendList(){
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        return CommonResult.success(userService.selectFriendList(Integer.parseInt(userId)));
    }

    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public CommonResult<Void> updateUser(@RequestBody UserVO userVO) {
        userService.updateUser(userVO);
        return CommonResult.success();
    }
}
