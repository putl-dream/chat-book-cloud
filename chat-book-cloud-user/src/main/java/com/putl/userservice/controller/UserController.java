package com.putl.userservice.controller;

import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.context.UserContext;
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

    @Operation(summary = "更新用户信息")
    @PostMapping("/update")
    public CommonResult<Void> updateUser(@RequestBody UserVO userVO) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        userService.updateUser(Integer.parseInt(userId), userVO);
        return CommonResult.success();
    }

    @Operation(summary = "根据ids批量查询用户")
    @PostMapping("/byIds")
    public CommonResult<List<UserVO>> getUsersByIds(@RequestBody List<Integer> ids) {
        List<UserVO> users = userService.selectByIds(ids);
        return CommonResult.success(users);
    }
}
