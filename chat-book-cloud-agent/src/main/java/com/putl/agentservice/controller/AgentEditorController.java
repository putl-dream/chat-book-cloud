package com.putl.agentservice.controller;

import com.putl.agentservice.model.dto.EditorAssistRequest;
import com.putl.agentservice.model.vo.EditorAssistResponse;
import com.putl.agentservice.service.EditorAssistService;
import fun.amireux.chat.book.framework.common.pojo.CommonResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Agent 编辑器助手")
@RestController
@RequestMapping("/agent/editor")
public class AgentEditorController {

    private final EditorAssistService editorAssistService;

    public AgentEditorController(EditorAssistService editorAssistService) {
        this.editorAssistService = editorAssistService;
    }

    @Operation(summary = "编辑器内续写或改写")
    @PostMapping("/assist")
    public CommonResult<EditorAssistResponse> assist(@RequestBody EditorAssistRequest request) {
        return CommonResult.success(editorAssistService.assist(request));
    }
}
