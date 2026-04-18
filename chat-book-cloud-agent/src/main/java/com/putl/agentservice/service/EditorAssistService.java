package com.putl.agentservice.service;

import com.putl.agentservice.model.dto.EditorAssistRequest;
import com.putl.agentservice.model.vo.EditorAssistResponse;

public interface EditorAssistService {

    EditorAssistResponse assist(EditorAssistRequest request);
}
