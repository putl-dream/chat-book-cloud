package com.putl.articleservice.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.Arrays;

@Data
@Builder
public class McpPrincipal {

    private Integer tokenId;

    private Integer userId;

    private String scopes;

    public boolean hasScope(String requiredScope) {
        if (!StringUtils.hasText(requiredScope) || !StringUtils.hasText(scopes)) {
            return false;
        }
        return Arrays.stream(scopes.split(","))
                .map(String::trim)
                .anyMatch(requiredScope::equals);
    }
}
