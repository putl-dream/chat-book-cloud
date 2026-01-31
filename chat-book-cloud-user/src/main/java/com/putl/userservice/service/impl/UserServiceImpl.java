package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.component.CaptchaService;
import com.putl.userservice.controller.vo.SignInVO;
import com.putl.userservice.controller.vo.UserChatVO;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.UserMapper;
import com.putl.userservice.mapper.entity.MessageDO;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.MessageService;
import com.putl.userservice.service.UserFriendsService;
import com.putl.userservice.service.UserInfoService;
import com.putl.userservice.service.UserService;
import com.putl.userservice.service.dto.LoginDTO;
import com.putl.userservice.util.JwtUtil;
import com.putl.userservice.util.PwdUtil;
import com.putl.userservice.util.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;
    private final UserInfoService userInfoService;
    private final CaptchaService captchaService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserFriendsService userFriendsService;
    private final MessageService messageService;

    @Override
    public UserVO selectById(int id){
        UserInfoDO userInfo = userInfoService.getByUserId(id);
        System.out.println("userInfo = " + userInfo);
        UserDO user = this.getById(id);
        String role = Objects.equals(userInfo.getRole(), RoleEnum.USER.getCode()) ? "user" : "admin";
        return UserVO.builder().id(userInfo.getId()).userId(id).username(userInfo.getUsername()).email(user.getEmail()).photo(userInfo.getPhoto()).profile(userInfo.getProfile()).role(role).build();
    }

    @Override
    public List<UserChatVO> selectFriendList(int userId){
        List<Integer> list = userFriendsService.getFriendList(userId);
        List<UserVO> userInfo = list.stream().map(this::selectById).toList();
        return userInfo.stream().map(userVO -> {
            MessageDO messageDO = messageService.queryLastMessage(userId, userVO.getUserId());
            System.err.println("messageDO = " + messageDO);
            if (messageDO == null) {
                messageDO = new MessageDO();
            }

            return UserChatVO.builder().id(userVO.getId()).userId(userVO.getUserId()).username(userVO.getUsername()).photo(userVO.getPhoto()).profile(userVO.getProfile()).lastChat(messageDO.getContent()).lastTime(messageDO.getSentTime()).build();
        }).toList();
    }

    /**
     * @param page
     * @param size
     * @return
     */
    @Override
    public IPage<UserVO> selectPage(Integer page, Integer size){
        Page<UserDO> user = userMapper.selectPage(new Page<>(page, size), Wrappers.<UserDO>lambdaQuery().orderByDesc(UserDO::getCreateTime));
        return user.convert(userDO -> selectById(userDO.getId()));
    }

    @Override
    public Result<String> signIn(SignInVO signInVO){
        // 校验验证码
        Boolean verifyResult = captchaService.verifyCaptcha(signInVO.getEmail(), signInVO.getCaptcha());
        if (verifyResult) return Result.error("验证码错误");

        // 注册用户
        String pwd = PwdUtil.hashPassword(signInVO.getPassword());
        UserDO user = UserDO.builder().email(signInVO.getEmail()).password(pwd).build();
        userMapper.insert(user);
        // 注册用户信息
        UserInfoDO userInfo = UserInfoDO.builder().userId(user.getId()).username(signInVO.getUsername()).build();
        // 保存用户信息
        userInfoService.save(userInfo);
        return Result.success("注册成功！");
    }

    public Result<String> signInText(String username, String email, String passwd){
        // 注册用户
        String pwd = PwdUtil.hashPassword(passwd);
        UserDO user = UserDO.builder().email(email).password(pwd).build();
        userMapper.insert(user);
        // 注册用户信息
        UserInfoDO userInfo = UserInfoDO.builder().userId(user.getId()).username(username).build();
        // 保存用户信息
        userInfoService.save(userInfo);
        return Result.success("注册成功！");
    }


    @Override
    public LoginDTO login(String email, String password){
        UserDO userDO = selectByEmail(email);
        boolean checked = PwdUtil.checkPassword(password, userDO.getPassword());
        if (!checked) return null;

        // 生成token
        String token = JwtUtil.generateToken(Map.of("id", userDO.getId(), "email", userDO.getEmail()));
        // 将token存入redis中，使用唯一的键格式
        String redisKey = "token:" + userDO.getId();
        redisTemplate.opsForValue().set(redisKey, token, JwtUtil.EXPIRATION_TIME, TimeUnit.SECONDS);
        // 返回包含 token 的 JSON 对象
        return new LoginDTO(token);
    }


    @Override
    public UserDO selectByEmail(String email){
        return userMapper.selectOne(Wrappers.<UserDO>lambdaQuery().eq(UserDO::getEmail, email));
    }
}
