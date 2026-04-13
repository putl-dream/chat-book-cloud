package com.putl.agentservice.client.engine;

public interface StreamingControl {

    boolean isCancelled();

    void onCancel(Runnable action);

    default void throwIfCancelled() {
        if (isCancelled()) {
            throw new StreamingCancelledException();
        }
    }

    static StreamingControl noop() {
        return NoopHolder.INSTANCE;
    }

    final class NoopHolder {
        private static final StreamingControl INSTANCE = new StreamingControl() {
            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void onCancel(Runnable action) {
                // no-op
            }
        };

        private NoopHolder() {
        }
    }
}
