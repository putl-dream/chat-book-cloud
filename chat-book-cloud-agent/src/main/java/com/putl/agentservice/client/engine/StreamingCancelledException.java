package com.putl.agentservice.client.engine;

public class StreamingCancelledException extends RuntimeException {

    public StreamingCancelledException() {
        super("Streaming invocation cancelled");
    }

    public StreamingCancelledException(String message) {
        super(message);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
