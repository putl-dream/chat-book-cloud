package com.putl.userservice.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.putl.articleservice.api.ArticleClient;
import com.putl.userservice.common.enums.RoleEnum;
import com.putl.userservice.controller.dto.UpdateRoleRequest;
import com.putl.userservice.controller.vo.DataCount;
import com.putl.userservice.controller.vo.UserVO;
import com.putl.userservice.mapper.AdminOperationLogMapper;
import com.putl.userservice.mapper.entity.AdminOperationLogDO;
import com.putl.userservice.service.UserService;
import fun.amireux.chat.book.framework.common.context.UserContext;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import fun.amireux.chat.book.framework.mvc.security.annotation.RequireAdmin;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Tag(name = "Admin API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/admin")
@RequireAdmin
public class AdminController {

    private final UserService userService;
    private final ArticleClient articleClient;
    private final AdminOperationLogMapper adminOperationLogMapper;

    @GetMapping("/count")
    public CommonResult<DataCount> getDataCount() {
        DataCount dataCount = new DataCount();
        dataCount.setUserCount(userService.count());
        CommonResult<Long> articleCountResult = articleClient.queryCount();
        CommonResult<Long> reviewCountResult = articleClient.queryPendingReviewCount();
        dataCount.setArticleCount(articleCountResult != null && articleCountResult.getData() != null
                ? articleCountResult.getData()
                : 0L);
        dataCount.setReviewCount(reviewCountResult != null && reviewCountResult.getData() != null
                ? reviewCountResult.getData()
                : 0L);
        return CommonResult.success(dataCount);
    }

    @GetMapping("/user")
    public CommonResult<IPage<UserVO>> getUserPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {
        return CommonResult.success(userService.selectPage(page, size, keyword, role, status));
    }

    @Operation(summary = "调整用户角色")
    @PutMapping("/{userId}/role")
    public CommonResult<Void> updateUserRole(
            @PathVariable Integer userId,
            @RequestBody @Valid UpdateRoleRequest request) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        Integer operatorId = Integer.parseInt(userIdStr);
        userService.updateUserRole(operatorId, userId,
                RoleEnum.valueOf(request.getRole().toUpperCase()));
        return CommonResult.success();
    }

    @Operation(summary = "禁用用户账号")
    @PutMapping("/{userId}/disable")
    public CommonResult<Void> disableUser(@PathVariable Integer userId) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        Integer operatorId = Integer.parseInt(userIdStr);
        userService.disableUser(operatorId, userId);
        return CommonResult.success();
    }

    @Operation(summary = "恢复用户账号")
    @PutMapping("/{userId}/enable")
    public CommonResult<Void> enableUser(@PathVariable Integer userId) {
        String userIdStr = UserContext.getUserId();
        if (userIdStr == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        Integer operatorId = Integer.parseInt(userIdStr);
        userService.enableUser(operatorId, userId);
        return CommonResult.success();
    }

    @Operation(summary = "管理员操作日志查询")
    @GetMapping("/operation-log/page")
    public CommonResult<IPage<AdminOperationLogDO>> getOperationLogPage(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer targetId,
            @RequestParam(required = false) Integer operatorId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        LambdaQueryWrapper<AdminOperationLogDO> w = new LambdaQueryWrapper<>();
        w.eq(action != null, AdminOperationLogDO::getAction, action);
        w.eq(targetType != null, AdminOperationLogDO::getTargetType, targetType);
        w.eq(targetId != null, AdminOperationLogDO::getTargetId, targetId);
        w.eq(operatorId != null, AdminOperationLogDO::getOperatorId, operatorId);
        w.ge(startTime != null, AdminOperationLogDO::getCreateTime, startTime);
        w.le(endTime != null, AdminOperationLogDO::getCreateTime, endTime);
        w.orderByDesc(AdminOperationLogDO::getCreateTime);
        return CommonResult.success(adminOperationLogMapper.selectPage(new Page<>(page, size), w));
    }
}
