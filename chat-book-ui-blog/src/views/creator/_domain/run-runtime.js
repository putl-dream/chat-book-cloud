export const AGENT_RUN_KIND = Object.freeze({
    CHAT: 'chat',
    DRAFT: 'draft'
});

export const AGENT_RUN_STATUS = Object.freeze({
    IDLE: 'idle',
    RUNNING: 'running',
    COMPLETED: 'completed',
    FAILED: 'failed',
    STOPPED: 'stopped'
});

export const AGENT_RUNTIME_EVENT = Object.freeze({
    MESSAGE_STARTED: 'message.started',
    MESSAGE_DELTA: 'message.delta',
    MESSAGE_COMPLETED: 'message.completed',
    MESSAGE_FAILED: 'message.failed',
    MESSAGE_STOPPED: 'message.stopped',
    ARTIFACT_STARTED: 'artifact.started',
    ARTIFACT_DELTA: 'artifact.delta',
    ARTIFACT_STATUS: 'artifact.status',
    ARTIFACT_COMPLETED: 'artifact.completed',
    ARTIFACT_FAILED: 'artifact.failed',
    ARTIFACT_STOPPED: 'artifact.stopped'
});

function toPreviewParts(previewText = '') {
    if (!previewText) {
        return [];
    }
    return [{
        type: 'text',
        content: previewText
    }];
}

function normalizePreviewText(previewText = '', previewParts = []) {
    if (typeof previewText === 'string' && previewText.length > 0) {
        return previewText;
    }
    return previewParts
        .filter((part) => part?.type === 'text' && typeof part.content === 'string')
        .map((part) => part.content)
        .join('');
}

export function createRunRuntime(overrides = {}) {
    const previewParts = Array.isArray(overrides.previewParts) ? overrides.previewParts : [];
    const previewText = normalizePreviewText(overrides.previewText ?? '', previewParts);
    return {
        runId: overrides.runId ?? null,
        runKind: overrides.runKind ?? null,
        status: overrides.status ?? AGENT_RUN_STATUS.IDLE,
        sessionId: overrides.sessionId ?? null,
        previewText,
        previewParts: previewParts.length > 0 ? previewParts : toPreviewParts(previewText),
        artifactBuffer: overrides.artifactBuffer ?? '',
        artifactPreview: overrides.artifactPreview ?? null,
        finalMessage: overrides.finalMessage ?? null,
        finalArtifact: overrides.finalArtifact ?? null,
        statusText: overrides.statusText ?? '',
        errorMessage: overrides.errorMessage ?? '',
        meta: {
            ...(overrides.meta || {})
        }
    };
}

export function resetRunRuntime(current = {}) {
    return createRunRuntime({
        runKind: current?.runKind ?? null
    });
}

export function startMessageRun(current = {}, options = {}) {
    return createRunRuntime({
        ...current,
        runId: options.runId ?? current?.runId ?? null,
        runKind: AGENT_RUN_KIND.CHAT,
        status: AGENT_RUN_STATUS.RUNNING,
        sessionId: options.sessionId ?? current?.sessionId ?? null,
        previewText: '',
        previewParts: [],
        finalMessage: null,
        statusText: options.statusText ?? '',
        errorMessage: '',
        meta: {
            ...(current?.meta || {}),
            messageId: options.messageId ?? current?.meta?.messageId ?? null,
            deltaCount: 0
        }
    });
}

export function appendMessagePreviewDelta(current = {}, delta = '') {
    const nextPreviewText = `${current?.previewText ?? ''}${delta || ''}`;
    const currentMeta = current?.meta || {};
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.CHAT,
        status: AGENT_RUN_STATUS.RUNNING,
        previewText: nextPreviewText,
        previewParts: toPreviewParts(nextPreviewText),
        meta: {
            ...currentMeta,
            deltaCount: Number(currentMeta.deltaCount || 0) + (delta ? 1 : 0)
        }
    });
}

export function completeMessageRun(current = {}, options = {}) {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.CHAT,
        status: AGENT_RUN_STATUS.COMPLETED,
        sessionId: options.sessionId ?? current?.sessionId ?? null,
        finalMessage: options.finalMessage ?? current?.finalMessage ?? null,
        statusText: options.statusText ?? current?.statusText ?? '',
        errorMessage: ''
    });
}

export function failMessageRun(current = {}, errorMessage = '') {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.CHAT,
        status: AGENT_RUN_STATUS.FAILED,
        errorMessage,
        statusText: errorMessage || current?.statusText || ''
    });
}

export function stopMessageRun(current = {}, statusText = '') {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.CHAT,
        status: AGENT_RUN_STATUS.STOPPED,
        statusText: statusText || current?.statusText || '',
        errorMessage: ''
    });
}

export function startArtifactRun(current = {}, options = {}) {
    return createRunRuntime({
        ...current,
        runId: options.runId ?? current?.runId ?? null,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.RUNNING,
        sessionId: options.sessionId ?? current?.sessionId ?? null,
        artifactBuffer: '',
        artifactPreview: null,
        finalArtifact: null,
        statusText: options.statusText ?? '',
        errorMessage: '',
        meta: {
            ...(current?.meta || {}),
            source: options.source ?? current?.meta?.source ?? null
        }
    });
}

export function updateArtifactStatus(current = {}, statusText = '') {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.RUNNING,
        statusText: statusText || current?.statusText || ''
    });
}

export function appendArtifactDelta(current = {}, options = {}) {
    const nextBuffer = `${current?.artifactBuffer ?? ''}${options.chunk ?? ''}`;
    const artifactPreview = typeof options.buildPreview === 'function'
        ? options.buildPreview(nextBuffer)
        : current?.artifactPreview ?? null;
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.RUNNING,
        artifactBuffer: nextBuffer,
        artifactPreview: artifactPreview ?? current?.artifactPreview ?? null,
        statusText: options.statusText ?? current?.statusText ?? ''
    });
}

export function completeArtifactRun(current = {}, options = {}) {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.COMPLETED,
        sessionId: options.sessionId ?? current?.sessionId ?? null,
        artifactPreview: options.artifactPreview ?? current?.artifactPreview ?? null,
        finalArtifact: options.finalArtifact ?? current?.finalArtifact ?? null,
        statusText: options.statusText ?? current?.statusText ?? '',
        errorMessage: ''
    });
}

export function failArtifactRun(current = {}, errorMessage = '') {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.FAILED,
        errorMessage,
        statusText: errorMessage || current?.statusText || ''
    });
}

export function stopArtifactRun(current = {}, statusText = '') {
    return createRunRuntime({
        ...current,
        runKind: AGENT_RUN_KIND.DRAFT,
        status: AGENT_RUN_STATUS.STOPPED,
        statusText: statusText || current?.statusText || ''
    });
}
