package com.putl.userservice.controller;


import fun.amireux.chat.book.framework.common.context.UserContext;
import com.putl.userservice.mapper.entity.MessageDO;
import com.putl.userservice.service.MessageService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import fun.amireux.chat.book.framework.common.pojo.ErrorType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "消息服务")
@RestController
@RequestMapping("/user/message")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "查询用户消息")
    @GetMapping("/queryUserMessage")
    public CommonResult<List<MessageDO>> queryUserMessage(Integer receiveId){
        String userId = UserContext.getUserId();
        if (userId == null) {
            return CommonResult.error(ErrorType.ERROR_401);
        }
        List<MessageDO> dos = messageService.queryUserMessage(Integer.parseInt(userId), receiveId);
        return CommonResult.success(dos);
    }

}
