package com.putl.articleservice.service;

import com.putl.articleservice.model.McpPrincipal;

public interface McpTokenService {

    McpPrincipal authenticate(String authorizationHeader, String requiredScope);
}
