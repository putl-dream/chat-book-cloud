package fun.amireux.chat.book.auth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import fun.amireux.chat.book.auth.projectobject.UserDO;
import fun.amireux.chat.book.auth.service.dto.UserDTO;

public interface UserService extends IService<UserDO> {
    /**
     * 登录
     *
     * @param user 登录信息
     * @return 登录结果
     */
    String login(UserDTO user);

    /**
     * 注册
     *
     * @return 注册结果
     */
    String signIn(UserDTO signInVO);

    /**
     * 获取用户信息
     *
     * @param userDTO 用户id
     * @return 用户信息
     */
    UserDO getUserInfo(UserDTO userDTO);
}