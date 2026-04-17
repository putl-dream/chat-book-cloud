package com.putl.agentservice.enums;

public enum DraftReadiness {
    NOT_READY,
    PARTIAL,
    READY;

    public static DraftReadiness safeValue(DraftReadiness value) {
        return value == null ? NOT_READY : value;
    }
}
