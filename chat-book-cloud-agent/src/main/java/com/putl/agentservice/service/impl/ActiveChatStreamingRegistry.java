package com.putl.agentservice.service.impl;

import com.putl.agentservice.client.engine.StreamingControl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@Component
public class ActiveChatStreamingRegistry {

    private final ConcurrentMap<String, ChatStreamingHandle> activeTasks = new ConcurrentHashMap<>();

    public ChatStreamingHandle register(String userId, Integer sessionId) {
        ChatStreamingHandle handle = new ChatStreamingHandle(userId, sessionId, key(userId, sessionId));
        ChatStreamingHandle previous = activeTasks.put(handle.getKey(), handle);
        if (previous != null) {
            previous.cancel("Superseded by a newer chat request");
        }
        return handle;
    }

    public ChatStreamingHandle cancel(String userId, Integer sessionId, String reason) {
        ChatStreamingHandle handle = activeTasks.remove(key(userId, sessionId));
        if (handle == null) {
            return null;
        }
        handle.cancel(reason);
        return handle;
    }

    public void complete(ChatStreamingHandle handle) {
        if (handle == null) {
            return;
        }
        activeTasks.remove(handle.getKey(), handle);
    }

    private String key(String userId, Integer sessionId) {
        return "%s:%s".formatted(userId, sessionId);
    }

    public static final class ChatStreamingHandle implements StreamingControl {

        private final String userId;
        private final Integer sessionId;
        private final String key;
        private final AtomicBoolean cancelled = new AtomicBoolean(false);
        private final AtomicReference<String> cancelReason = new AtomicReference<>("");
        private final CopyOnWriteArrayList<Runnable> cancelCallbacks = new CopyOnWriteArrayList<>();

        private ChatStreamingHandle(String userId, Integer sessionId, String key) {
            this.userId = userId;
            this.sessionId = sessionId;
            this.key = key;
        }

        public String getUserId() {
            return userId;
        }

        public Integer getSessionId() {
            return sessionId;
        }

        public String getKey() {
            return key;
        }

        public String getCancelReason() {
            return cancelReason.get();
        }

        public boolean cancel(String reason) {
            if (StringUtils.hasText(reason)) {
                cancelReason.compareAndSet("", reason);
            }
            if (!cancelled.compareAndSet(false, true)) {
                return false;
            }
            cancelCallbacks.forEach(this::runQuietly);
            cancelCallbacks.clear();
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public void onCancel(Runnable action) {
            if (action == null) {
                return;
            }
            if (cancelled.get()) {
                runQuietly(action);
                return;
            }
            cancelCallbacks.add(action);
            if (cancelled.get() && cancelCallbacks.remove(action)) {
                runQuietly(action);
            }
        }

        private void runQuietly(Runnable action) {
            try {
                action.run();
            } catch (Exception ignored) {
                // best-effort cancellation hooks should not break caller flow
            }
        }
    }
}
