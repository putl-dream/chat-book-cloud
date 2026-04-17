package com.putl.agentservice.enums;

import java.util.Locale;

public enum AgentSceneType {
    CREATE,
    OPTIMIZE,
    DISCUSS,
    LEARN,
    DRAFT,
    EDIT;

    public AgentSceneType toRuntimeScene() {
        return switch (this) {
            case CREATE -> DISCUSS;
            case OPTIMIZE -> EDIT;
            default -> this;
        };
    }

    public boolean isLegacyScene() {
        return this == CREATE || this == OPTIMIZE;
    }

    public static AgentSceneType initialScene(AgentSceneType requestedScene, Integer targetDraftId) {
        if (requestedScene != null) {
            return requestedScene.toRuntimeScene();
        }
        if (targetDraftId != null && targetDraftId > 0) {
            return EDIT;
        }
        return DISCUSS;
    }

    public static AgentSceneType fromValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return AgentSceneType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
