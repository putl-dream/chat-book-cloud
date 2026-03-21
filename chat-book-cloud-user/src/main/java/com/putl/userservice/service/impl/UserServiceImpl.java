package com.putl.userservice.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.UserInfoMapper;
import com.putl.userservice.mapper.UserMapper;
import com.putl.userservice.mapper.entity.UserDO;
import com.putl.userservice.mapper.entity.UserInfoDO;
import com.putl.userservice.service.UserInfoService;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.context.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    private final UserMapper userMapper;
    private final UserInfoService userInfoService;
    private final UserInfoMapper userInfoMapper;

    @Override
    @Cacheable(value = "userCache", key = "#id", unless = "#result == null")
    public UserVO selectById(int id) {
        UserInfoDO userInfo = userInfoService.getByUserId(id);
        log.debug("userInfo = {}", userInfo);
        UserDO user = this.getById(id);
        if (userInfo == null) {
            throw new RuntimeException("用户信息未找到(user_info缺失)，请联系管理员");
        }
        if (user == null) {
            throw new RuntimeException("用户账号信息未找到(user缺失)，请联系管理员");
        }
        String role = (userInfo.getRole() == RoleEnum.USER) ? "user" : "admin";
        return UserVO.builder()
                .id(userInfo.getId())
                .userId(id)
                .username(userInfo.getUsername())
                .email(user.getEmail())
                .photo(userInfo.getPhoto())
                .profile(userInfo.getProfile())
                .role(role)
                .build();
    }

    @Override
    @Cacheable(value = "userBatchCache", key = "T(java.util.Arrays).hashCode(#ids.![].toArray())", unless = "#result == null || #result.isEmpty()")
    public List<UserVO> selectByIds(List<Integer> ids) {
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        // 批量查询用户信息
        List<UserInfoDO> userInfos = userInfoMapper.selectList(
                Wrappers.<UserInfoDO>lambdaQuery().in(UserInfoDO::getUserId, ids));
        Map<Integer, UserInfoDO> userInfoMap = userInfos.stream()
                .collect(Collectors.toMap(UserInfoDO::getUserId, Function.identity()));

        // 批量查询账号信息
        List<UserDO> users = this.listByIds(ids);
        Map<Integer, UserDO> userMap = users.stream()
                .collect(Collectors.toMap(UserDO::getId, Function.identity()));

        // 组装结果
        List<UserVO> result = new ArrayList<>();
        for (Integer id : ids) {
            UserInfoDO userInfo = userInfoMap.get(id);
            UserDO user = userMap.get(id);
            if (userInfo == null || user == null) {
                continue;
            }
            String role = (userInfo.getRole() == RoleEnum.USER) ? "user" : "admin";
            result.add(UserVO.builder()
                    .id(userInfo.getId())
                    .userId(id)
                    .username(userInfo.getUsername())
                    .email(user.getEmail())
                    .photo(userInfo.getPhoto())
                    .profile(userInfo.getProfile())
                    .role(role)
                    .build());
        }
        return result;
    }

    @Override
    public IPage<UserVO> selectPage(Integer page, Integer size) {
        Page<UserDO> userPage = userMapper.selectPage(new Page<>(page, size), Wrappers.<UserDO>lambdaQuery()
                .orderByDesc(UserDO::getCreateTime));
        List<Integer> ids = userPage.getRecords().stream().map(UserDO::getId).toList();
        List<UserVO> userVOList = selectByIds(ids);
        Map<Integer, UserVO> userVOMap = userVOList.stream()
                .collect(Collectors.toMap(UserVO::getUserId, Function.identity()));
        // 保持原有分页顺序
        List<UserVO> orderedList = userPage.getRecords().stream()
                .map(record -> userVOMap.get(record.getId()))
                .filter(Objects::nonNull)
                .toList();
        Page<UserVO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        resultPage.setRecords(orderedList);
        return resultPage;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "userCache", key = "#userVO.userId"),
            @CacheEvict(value = "userBatchCache", allEntries = true)
    })
    public void updateUser(UserVO userVO) {
        String userId = UserContext.getUserId();
        if (userId == null) {
            throw new IllegalStateException("用户信息未找到，请重新登录");
        }
        Integer currentUserId = Integer.parseInt(userId);

        UserInfoDO existing = userInfoService.getByUserId(currentUserId);
        UserInfoDO duplicateUsername = userInfoMapper.selectOne(Wrappers.<UserInfoDO>lambdaQuery()
                .eq(UserInfoDO::getUsername, userVO.getUsername())
                .ne(UserInfoDO::getUserId, currentUserId));
        if (duplicateUsername != null) {
            throw new IllegalArgumentException("用户名已存在");
        }

        if (existing == null) {
            userInfoService.save(UserInfoDO.builder()
                    .userId(currentUserId)
                    .username(userVO.getUsername())
                    .photo(userVO.getPhoto())
                    .profile(userVO.getProfile())
                    .role(RoleEnum.USER)
                    .build());
            return;
        }

        existing.setUsername(userVO.getUsername());
        existing.setPhoto(userVO.getPhoto());
        existing.setProfile(userVO.getProfile());
        userInfoService.update(existing);
    }
}
