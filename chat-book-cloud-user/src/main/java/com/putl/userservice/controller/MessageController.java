package com.putl.userservice.controller;


import com.putl.userservice.common.ReqInfoContext;
import com.putl.userservice.mapper.entity.MessageDO;
import com.putl.userservice.service.MessageService;
import com.putl.userservice.util.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "消息服务")
@RestController
@RequestMapping("/user/captcha")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @Operation(summary = "查询用户消息")
    @GetMapping("/queryUserMessage")
    public Result<List<MessageDO>> queryUserMessage(Integer receiveId){
        List<MessageDO> dos = messageService.queryUserMessage(ReqInfoContext.getReqInfo().getUserId(), receiveId);
        return Result.success(dos);
    }

}
