package com.putl.agentservice.constants;

public final class AgentStreamEventConstants {

    private AgentStreamEventConstants() {
    }

    public static final String MESSAGE_CREATE = "message.create";
    public static final String MESSAGE_STOP = "message.stop";
    public static final String MESSAGE_STARTED = "message.started";
    public static final String MESSAGE_DELTA = "message.delta";
    public static final String MESSAGE_COMPLETED = "message.completed";
    public static final String MESSAGE_FAILED = "message.failed";
    public static final String MESSAGE_STOPPED = "message.stopped";

    public static final String ARTIFACT_GENERATE = "artifact.generate";
    public static final String ARTIFACT_STARTED = "artifact.started";
    public static final String ARTIFACT_STATUS = "artifact.status";
    public static final String ARTIFACT_DELTA = "artifact.delta";
    public static final String ARTIFACT_COMPLETED = "artifact.completed";
    public static final String ARTIFACT_FAILED = "artifact.failed";
    public static final String ARTIFACT_STOPPED = "artifact.stopped";
    public static final String ARTIFACT_STOP = "artifact.stop";
}
