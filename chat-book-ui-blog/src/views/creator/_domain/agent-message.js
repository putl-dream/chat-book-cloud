import {
    AGENT_MESSAGE_TYPE,
    normalizeAgentMessageType,
    normalizeMessagePayload
} from '@/views/creator/_domain/agent-interaction.js';

export function normalizeMessageRole(role) {
    const roleMap = {
        USER: 'user',
        ASSISTANT: 'assistant',
        SYSTEM: 'system'
    };
    return roleMap[role] || 'assistant';
}

export function normalizeMessage(message = {}) {
    return {
        id: message.id ?? `${Date.now()}-${Math.random()}`,
        role: normalizeMessageRole(message.role),
        messageType: normalizeAgentMessageType(message.messageType),
        content: message.content ?? '',
        payload: normalizeMessagePayload(message.messageType, message.payload),
        createTime: message.createTime ?? '',
        streaming: Boolean(message.streaming)
    };
}

export function resolveCompletedStreamingMessage(current = {}, completionPayload = {}, previewText = '') {
    const resolvedPreviewText = typeof completionPayload?.previewText === 'string'
        ? completionPayload.previewText
        : String(previewText || '');
    const stableMessageId = current?.id ?? completionPayload?.finalMessage?.id;

    if (completionPayload?.finalMessage && typeof completionPayload.finalMessage === 'object') {
        const normalized = normalizeMessage({
            ...completionPayload.finalMessage,
            id: stableMessageId,
            role: completionPayload.finalMessage.role ?? 'ASSISTANT'
        });
        const shouldUsePreviewFallback = normalized.messageType === AGENT_MESSAGE_TYPE.TEXT
            && (!normalized.content || completionPayload?.telemetry?.previewFallbackApplied);
        if (shouldUsePreviewFallback && resolvedPreviewText) {
            return {
                ...normalized,
                content: resolvedPreviewText
            };
        }
        return normalized;
    }

    return normalizeMessage({
        id: stableMessageId,
        role: 'ASSISTANT',
        messageType: AGENT_MESSAGE_TYPE.TEXT,
        content: resolvedPreviewText || current.content
    });
}
