package com.putl.userservice.controller;

import fun.amireux.chat.book.framework.common.context.UserContext;
import com.putl.userservice.controller.vo.LoginVO;
import com.putl.userservice.controller.vo.SignInVO;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.service.UserService;
import com.putl.userservice.service.dto.LoginDTO;
import com.putl.userservice.util.Result;
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

    @PostMapping("/login")
    public Result<LoginDTO> login(@RequestBody LoginVO loginVO){
        LoginDTO dto = userService.login(loginVO.getEmail(), loginVO.getPassword());
        return dto == null ? Result.error("用户名或密码错误") : Result.success(dto);
    }

    @PostMapping("/signIn")
    public Result<String> signIn(@RequestBody SignInVO signInVO){
        if (userService.selectByEmail(signInVO.getEmail()) != null) {
            return Result.error("用户名已存在");
        }
        return userService.signIn(signInVO);
    }

    @Operation(summary = "根据id查询用户")
    @GetMapping("/byId")
    public Result<UserVO> getUserById(@RequestParam Integer id){
        UserVO user = userService.selectById(id);
        return Result.success(user);
    }

    @Operation(summary = "查询自己")
    @GetMapping("/bySelf")
    public Result<UserVO> getUserBySelf(){
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户信息未找到，请重新登录");
        }
        UserVO user = userService.selectById(Integer.parseInt(userId));
        return Result.success(user);
    }

    @Operation(summary = "根据id查询用户好友列表")
    @GetMapping("/friendList")
    public Result<List<UserChatVO>> getFriendList(){
        String userId = UserContext.getUserId();
        if (userId == null) {
            return Result.error("用户信息未找到，请重新登录");
        }
        return Result.success(userService.selectFriendList(Integer.parseInt(userId)));
    }
}
