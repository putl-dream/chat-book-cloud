package com.putl.agentservice.constants;

public final class AgentStreamEventConstants {

    private AgentStreamEventConstants() {
    }

    public static final String AGENT_CHAT = "AGENT_CHAT";
    public static final String AGENT_CHAT_START = "AGENT_CHAT_START";
    public static final String AGENT_CHAT_DELTA = "AGENT_CHAT_DELTA";
    public static final String AGENT_CHAT_DONE = "AGENT_CHAT_DONE";
    public static final String AGENT_CHAT_ERROR = "AGENT_CHAT_ERROR";

    public static final String AGENT_DRAFT_GENERATE = "AGENT_DRAFT_GENERATE";
    public static final String AGENT_DRAFT_GENERATE_START = "AGENT_DRAFT_GENERATE_START";
    public static final String AGENT_DRAFT_GENERATE_STATUS = "AGENT_DRAFT_GENERATE_STATUS";
    public static final String AGENT_DRAFT_GENERATE_DELTA = "AGENT_DRAFT_GENERATE_DELTA";
    public static final String AGENT_DRAFT_GENERATE_DONE = "AGENT_DRAFT_GENERATE_DONE";
    public static final String AGENT_DRAFT_GENERATE_ERROR = "AGENT_DRAFT_GENERATE_ERROR";
    public static final String AGENT_DRAFT_GENERATE_STOPPED = "AGENT_DRAFT_GENERATE_STOPPED";
    public static final String AGENT_DRAFT_GENERATE_STOP = "AGENT_DRAFT_GENERATE_STOP";
}
