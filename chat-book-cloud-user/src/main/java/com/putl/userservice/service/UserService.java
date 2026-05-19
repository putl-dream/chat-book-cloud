package com.putl.userservice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.entity.UserDO;

import java.util.List;

/**
 * 用户服务接口
 * <p>提供用户管理的核心业务功能，包括用户查询、更新、角色管理等</p>
 *
 * @author ChatBook Cloud Team
 * @since 1.0.0
 */
public interface UserService extends IService<UserDO> {
    
    /**
     * 根据ID查询用户信息
     *
     * @param id 用户ID
     * @return 用户视图对象
     */
    UserVO selectById(int id);

    /**
     * 根据IDs批量查询用户信息
     *
     * @param ids 用户ID列表
     * @return 用户视图对象列表
     */
    List<UserVO> selectByIds(List<Integer> ids);

    /**
     * 分页查询用户列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param keyword 搜索关键词
     * @param role 角色过滤
     * @param status 状态过滤
     * @return 分页结果
     */
    IPage<UserVO> selectPage(Integer page, Integer size, String keyword, String role, Integer status);

    /**
     * 更新用户信息
     *
     * @param currentUserId 当前登录用户ID
     * @param userVO 用户视图对象，包含待更新的信息
     */
    void updateUser(Integer currentUserId, UserVO userVO);

    /**
     * 调整用户角色（管理员功能）
     *
     * @param operatorId 操作者ID
     * @param targetUserId 目标用户ID
     * @param roleEnum 目标角色枚举
     */
    void updateUserRole(Integer operatorId, Integer targetUserId, RoleEnum roleEnum);

    /**
     * 禁用用户账号（管理员功能）
     *
     * @param operatorId 操作者ID
     * @param targetUserId 目标用户ID
     */
    void disableUser(Integer operatorId, Integer targetUserId);

    /**
     * 恢复用户账号（管理员功能）
     *
     * @param operatorId 操作者ID
     * @param targetUserId 目标用户ID
     */
    void enableUser(Integer operatorId, Integer targetUserId);
}
