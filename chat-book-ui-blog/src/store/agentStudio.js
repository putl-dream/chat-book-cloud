import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';

import { API_CONFIG } from '@/config/index.js';
import { getAccessToken } from '@/utils/token.js';
import SocketService, { formatWsUrl } from '@/utils/websocket.js';
import {
    AGENT_ASSISTANT_ACTION,
    AGENT_DRAFT_READINESS,
    AGENT_SCENE_TYPE,
    adoptAgentDraftVersion,
    buildStreamingDraftPreview,
    createAgentSession,
    getAgentSessionDetail,
    getAgentSessionPage,
    normalizeAssistantAction,
    normalizeAgentDraft,
    normalizeDraftReadiness,
    normalizeSceneType,
    optimizeAgentDraft,
    saveAgentGenerationIntent,
    saveAgentDraftImport
} from '@/views/creator/_domain/agent.js';
import {
    AGENT_MESSAGE_TYPE,
    buildInteractionResponsePayload,
    buildInteractionResponseSummary,
    extractInteractionResponse,
    isInteractionResponseMessage,
    normalizeAgentMessageType,
    normalizeMessagePayload
} from '@/views/creator/_domain/agent-interaction.js';
import {
    AGENT_RUNTIME_COMMAND
} from '@/views/creator/_domain/stream-constants.js';
import {
    PREVIEW_PLAYBACK_INTERVAL_MS,
    consumePreviewPlaybackTokens,
    tokenizePreviewDelta
} from '@/views/creator/_domain/preview-playback.js';
import {
    AGENT_RUN_KIND,
    AGENT_RUN_STATUS,
    AGENT_RUNTIME_EVENT,
    appendArtifactDelta,
    appendMessagePreviewDelta,
    completeArtifactRun,
    completeMessageRun,
    createRunRuntime,
    failArtifactRun,
    failMessageRun,
    resetRunRuntime,
    startArtifactRun,
    startMessageRun,
    stopArtifactRun,
    updateArtifactStatus
} from '@/views/creator/_domain/run-runtime.js';
import router from '@/router/index.js';

const DEFAULT_SESSION_TITLE = '新的 AI 创作会话';
const DEFAULT_HISTORY_PAGE_SIZE = 12;

const SCENE_LABELS = Object.freeze({
    [AGENT_SCENE_TYPE.DISCUSS]: '讨论共创',
    [AGENT_SCENE_TYPE.LEARN]: '学习整理',
    [AGENT_SCENE_TYPE.DRAFT]: '首稿生成',
    [AGENT_SCENE_TYPE.EDIT]: '智能编辑'
});

const SCENE_SUBTITLES = Object.freeze({
    [AGENT_SCENE_TYPE.DISCUSS]: '围绕主题、论点和事实边界展开讨论，逐步收敛写作方向。',
    [AGENT_SCENE_TYPE.LEARN]: '聚焦概念解释、知识点梳理和例子拆解，把知识转成可写素材。',
    [AGENT_SCENE_TYPE.DRAFT]: '当前材料已接近成稿条件，可以进入首稿生成并开始搭建正文。',
    [AGENT_SCENE_TYPE.EDIT]: '已有草稿后，继续围绕润色、改写、扩写和补全推进。'
});

function normalizeMessageRole(role) {
    const roleMap = {
        USER: 'user',
        ASSISTANT: 'assistant',
        SYSTEM: 'system'
    };
    return roleMap[role] || 'assistant';
}

function normalizeMessage(message = {}) {
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

function normalizeNotebook(notebook = {}) {
    return {
        ...notebook,
        currentScene: normalizeSceneType(notebook.currentScene, AGENT_SCENE_TYPE.DISCUSS),
        draftReadiness: normalizeDraftReadiness(notebook.draftReadiness, AGENT_DRAFT_READINESS.NOT_READY),
        nextSuggestedAction: normalizeAssistantAction(notebook.nextSuggestedAction, AGENT_ASSISTANT_ACTION.ASK)
    };
}

function normalizeSessionSummary(sessionRecord = {}) {
    return {
        id: sessionRecord.id ? Number(sessionRecord.id) : null,
        title: sessionRecord.title ?? DEFAULT_SESSION_TITLE,
        sceneType: normalizeSceneType(sessionRecord.sceneType, AGENT_SCENE_TYPE.DISCUSS),
        status: sessionRecord.status ?? 'ACTIVE',
        targetDraftId: sessionRecord.targetDraftId ?? null,
        createTime: sessionRecord.createTime ?? '',
        updateTime: sessionRecord.updateTime ?? sessionRecord.createTime ?? ''
    };
}

function toSessionTimestamp(value) {
    if (!value) {
        return 0;
    }
    const parsed = new Date(value).getTime();
    return Number.isFinite(parsed) ? parsed : 0;
}

function sortSessionHistory(list = []) {
    return [...list].sort((left, right) =>
        toSessionTimestamp(right.updateTime) - toSessionTimestamp(left.updateTime)
        || (Number(right.id) || 0) - (Number(left.id) || 0));
}

function mergeSessionHistory(existingList = [], incomingList = []) {
    const mergedMap = new Map();
    [...existingList, ...incomingList].forEach((item) => {
        if (item?.id) {
            mergedMap.set(item.id, item);
        }
    });
    return sortSessionHistory([...mergedMap.values()]);
}

function matchesSessionKeyword(title, keyword = '') {
    const normalizedKeyword = String(keyword || '').trim().toLowerCase();
    if (!normalizedKeyword) {
        return true;
    }
    return String(title || '').toLowerCase().includes(normalizedKeyword);
}

export const useAgentStudioStore = defineStore('agentStudio', () => {
    // Basic States
    const loadingSession = ref(false);
    const creatingSession = ref(false);
    const optimizingDraft = ref(false);
    const adoptingCandidate = ref(false);

    // Context
    const session = ref(null);
    const sessionId = ref(null);
    const sessionTitle = ref(DEFAULT_SESSION_TITLE);
    const messages = ref([]);
    const notebook = ref(null);
    const currentScene = ref(AGENT_SCENE_TYPE.DISCUSS);
    const nextScene = ref(AGENT_SCENE_TYPE.DISCUSS);
    const draftReadiness = ref(AGENT_DRAFT_READINESS.NOT_READY);
    const assistantAction = ref(AGENT_ASSISTANT_ACTION.ASK);
    const switchReason = ref('');

    const sessionHistory = ref([]);
    const sessionHistoryLoading = ref(false);
    const sessionHistoryLoadingMore = ref(false);
    const sessionHistoryTotal = ref(0);
    const sessionHistoryKeyword = ref('');
    const sessionHistoryPageNo = ref(0);
    const sessionHistoryPageSize = ref(DEFAULT_HISTORY_PAGE_SIZE);
    
    // Draft states
    const draft = ref(null);
    const candidateDraft = ref(null);
    const chatRun = ref(createRunRuntime({ runKind: AGENT_RUN_KIND.CHAT }));
    const draftRun = ref(createRunRuntime({ runKind: AGENT_RUN_KIND.DRAFT }));

    // WebSocket Internals (not reactive)
    let socketService = null;
    let socketReadyPromise = null;
    let closingSocket = false;
    let sessionHistoryRequestSerial = 0;
    let queuedPreviewTokens = [];
    let previewPlaybackTimer = null;
    let pendingCompletionPayload = null;

    // Computed
    const interactionResponseMap = computed(() => {
        const map = new Map();
        messages.value.forEach((message) => {
            const interactionResponse = extractInteractionResponse(message);
            if (interactionResponse?.formId) {
                map.set(interactionResponse.formId, interactionResponse);
            }
        });
        return map;
    });

    const visibleMessages = computed(() => messages.value
        .filter((message) => !isInteractionResponseMessage(message))
        .map((message) => {
            const projectedMessage = projectVisibleMessage(message);
            if (projectedMessage.role === 'assistant' && projectedMessage.messageType === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM) {
                const formId = projectedMessage.payload?.formId;
                return {
                    ...projectedMessage,
                    interactionResponse: formId ? (interactionResponseMap.value.get(formId) ?? null) : null
                };
            }
            return projectedMessage;
        }));

    const hasMessages = computed(() => visibleMessages.value.length > 0);
    const hasDraft = computed(() => Boolean(draft.value?.draftId));
    const hasCandidateDraft = computed(() => Boolean(candidateDraft.value?.versionNo));
    const isDraftReady = computed(() => draftReadiness.value === AGENT_DRAFT_READINESS.READY);
    const hasPendingInteractiveForm = computed(() => visibleMessages.value.some((message) =>
        message.role === 'assistant'
        && message.messageType === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM
        && !message.interactionResponse
    ));
    const currentSceneLabel = computed(() => SCENE_LABELS[currentScene.value] || '讨论共创');
    const nextSceneLabel = computed(() => SCENE_LABELS[nextScene.value] || currentSceneLabel.value);
    const currentSceneSubtitle = computed(() => SCENE_SUBTITLES[currentScene.value] || SCENE_SUBTITLES[AGENT_SCENE_TYPE.DISCUSS]);
    const sceneFooterHint = computed(() => {
        if (hasPendingInteractiveForm.value) {
            return '请先完成上方问题卡片，Agent 会在收到完整答案后继续推进当前场景。';
        }
        if (currentScene.value === AGENT_SCENE_TYPE.LEARN) {
            return isDraftReady.value
                ? '知识点已逐步沉淀，可以继续学习，也可以切换到首稿生成。'
                : '当前以知识解释和梳理为主，先把关键概念讲透。';
        }
        if (currentScene.value === AGENT_SCENE_TYPE.EDIT) {
            return '当前已进入编辑语境，可以继续描述你想润色、改写或补全的目标。';
        }
        if (currentScene.value === AGENT_SCENE_TYPE.DRAFT) {
            return '当前材料已接近成稿条件，可以直接进入首稿生成，再回到编辑场景继续修改。';
        }
        if (isDraftReady.value) {
            return '讨论信息已经比较完整，可以继续打磨，也可以开始生成首稿。';
        }
        return '当前以讨论为主，先补齐主题、事实和结构，再进入首稿生成。';
    });
    const generateButtonLabel = computed(() => {
        if (currentScene.value === AGENT_SCENE_TYPE.EDIT || hasDraft.value) {
            return '继续生成首稿';
        }
        if (isDraftReady.value || nextScene.value === AGENT_SCENE_TYPE.DRAFT || assistantAction.value === AGENT_ASSISTANT_ACTION.SUGGEST_DRAFT) {
            return '进入首稿生成';
        }
        return '生成初稿';
    });
    
    // Core Status Enum computed for the Middle Canvas UI (DraftCanvas)
    // Values: 'empty' | 'generating' | 'completed' | 'optimizing'
    const draftStatus = computed(() => {
        if (generatingDraft.value) return 'generating';
        if (optimizingDraft.value) return 'optimizing';
        if (hasCandidateDraft.value || hasDraft.value) return 'completed';
        return 'empty';
    });
    
    const displayDraft = computed(() => {
        // Return candidate if exists to do "seamless overlay" display
        return candidateDraft.value || draft.value || null;
    });
    const streamingDraftPreview = computed(() => {
        if (!generatingDraft.value) {
            return null;
        }
        return draftRun.value.artifactPreview;
    });

    const activeDraftVersion = computed(() => draft.value?.versionNo ?? null);
    const pendingDraftVersion = computed(() => candidateDraft.value?.versionNo ?? null);
    const hasSessionHistory = computed(() => sessionHistory.value.length > 0);
    const hasMoreSessionHistory = computed(() => sessionHistory.value.length < sessionHistoryTotal.value);
    const chatting = computed(() => chatRun.value.status === AGENT_RUN_STATUS.RUNNING);
    const generatingDraft = computed(() => draftRun.value.status === AGENT_RUN_STATUS.RUNNING);
    const draftStreamingStatusText = computed(() => draftRun.value.statusText || '');

    const sessionStatusLabel = computed(() => {
        if (loadingSession.value) return '正在恢复';
        if (chatting.value) return `${currentSceneLabel.value}中`;
        if (generatingDraft.value) return '正在跳转编辑器';
        if (optimizingDraft.value) return '优化重写中';
        if (sessionId.value) return `${currentSceneLabel.value}已激活`;
        return '尚未开始讨论';
    });

    // Message Utilities
    const findMessageIndex = (messageId) => messages.value.findIndex((item) => item.id === messageId);

    const cancelPreviewPlayback = () => {
        if (previewPlaybackTimer == null) {
            return;
        }
        clearTimeout(previewPlaybackTimer);
        previewPlaybackTimer = null;
    };

    const resetPreviewPlayback = () => {
        cancelPreviewPlayback();
        queuedPreviewTokens = [];
        pendingCompletionPayload = null;
    };

    const schedulePreviewPlayback = () => {
        if (previewPlaybackTimer != null || queuedPreviewTokens.length === 0) {
            return;
        }
        previewPlaybackTimer = setTimeout(() => {
            previewPlaybackTimer = null;
            flushPreviewPlayback();
        }, PREVIEW_PLAYBACK_INTERVAL_MS);
    };

    const flushPreviewPlayback = () => {
        const streamingAssistantMessageId = getStreamingAssistantMessageId();
        if (!streamingAssistantMessageId || findMessageIndex(streamingAssistantMessageId) < 0) {
            resetPreviewPlayback();
            return;
        }

        const delta = consumePreviewPlaybackTokens(queuedPreviewTokens);
        if (delta) {
            chatRun.value = appendMessagePreviewDelta(chatRun.value, delta);
        }

        if (queuedPreviewTokens.length > 0) {
            schedulePreviewPlayback();
            return;
        }

        if (pendingCompletionPayload) {
            const completionPayload = pendingCompletionPayload;
            pendingCompletionPayload = null;
            finishStreamingMessage(completionPayload);
        }
    };

    const enqueuePreviewDelta = (delta = '') => {
        const nextTokens = tokenizePreviewDelta(delta);
        if (!nextTokens.length) {
            return;
        }
        queuedPreviewTokens.push(...nextTokens);
        schedulePreviewPlayback();
    };

    const hasPendingPreviewPlayback = () => queuedPreviewTokens.length > 0 || previewPlaybackTimer != null;

    const clearStreamingState = ({ resetRuntime = false } = {}) => {
        resetPreviewPlayback();
        if (resetRuntime) {
            chatRun.value = resetRunRuntime(chatRun.value);
            return;
        }
        chatRun.value = createRunRuntime({
            ...chatRun.value,
            previewText: '',
            previewParts: [],
            meta: {
                ...(chatRun.value.meta || {}),
                messageId: null,
                deltaCount: 0
            }
        });
    };

    const upsertSessionHistory = (source = {}, options = {}) => {
        const normalized = normalizeSessionSummary(source);
        if (!normalized.id) {
            return;
        }

        const {
            ignoreFilter = false,
            removeWhenFiltered = false
        } = options;

        const matchesKeyword = ignoreFilter || matchesSessionKeyword(normalized.title, sessionHistoryKeyword.value);
        if (!matchesKeyword) {
            if (removeWhenFiltered) {
                sessionHistory.value = sessionHistory.value.filter((item) => item.id !== normalized.id);
            }
            return;
        }

        const existing = sessionHistory.value.find((item) => item.id === normalized.id) ?? {};
        sessionHistory.value = sortSessionHistory([
            {
                ...existing,
                ...normalized
            },
            ...sessionHistory.value.filter((item) => item.id !== normalized.id)
        ]);
    };

    const syncActiveSessionHistory = (overrides = {}, options = {}) => {
        if (!sessionId.value) {
            return;
        }
        upsertSessionHistory({
            id: sessionId.value,
            title: overrides.title ?? sessionTitle.value ?? session.value?.title ?? DEFAULT_SESSION_TITLE,
            sceneType: overrides.sceneType ?? currentScene.value ?? session.value?.sceneType,
            status: overrides.status ?? session.value?.status ?? 'ACTIVE',
            targetDraftId: overrides.targetDraftId ?? session.value?.targetDraftId ?? null,
            createTime: overrides.createTime ?? session.value?.createTime ?? '',
            updateTime: overrides.updateTime ?? new Date().toISOString()
        }, options);
    };

    const applyScenePayload = (payload = {}, sourceNotebook = null) => {
        const normalizedNotebook = sourceNotebook ? normalizeNotebook(sourceNotebook) : null;
        notebook.value = normalizedNotebook ?? notebook.value;
        currentScene.value = normalizeSceneType(
            payload.currentScene ?? normalizedNotebook?.currentScene ?? session.value?.sceneType,
            AGENT_SCENE_TYPE.DISCUSS
        );
        nextScene.value = normalizeSceneType(
            payload.nextScene ?? currentScene.value,
            currentScene.value
        );
        draftReadiness.value = normalizeDraftReadiness(
            payload.draftReadiness ?? normalizedNotebook?.draftReadiness,
            draftReadiness.value
        );
        assistantAction.value = normalizeAssistantAction(
            payload.assistantAction ?? normalizedNotebook?.nextSuggestedAction,
            assistantAction.value
        );
        switchReason.value = payload.switchReason ?? switchReason.value ?? '';

        if (session.value) {
            session.value.sceneType = currentScene.value;
        }
        if (notebook.value) {
            notebook.value.currentScene = currentScene.value;
            notebook.value.draftReadiness = draftReadiness.value;
            notebook.value.nextSuggestedAction = assistantAction.value;
        }
    };

    const resetDraftStreamingState = ({ resetRuntime = true } = {}) => {
        if (resetRuntime) {
            draftRun.value = resetRunRuntime(draftRun.value);
            return;
        }
        draftRun.value = createRunRuntime({
            ...draftRun.value,
            artifactBuffer: '',
            artifactPreview: null
        });
    };

    const getStreamingAssistantMessageId = () => chatRun.value.meta?.messageId ?? null;
    const getStreamingPreviewText = () => String(chatRun.value.previewText || '');

    const projectVisibleMessage = (message) => {
        if (!message?.streaming || message.id !== getStreamingAssistantMessageId()) {
            return message;
        }
        return {
            ...message,
            content: getStreamingPreviewText(),
            previewText: getStreamingPreviewText(),
            previewParts: chatRun.value.previewParts
        };
    };

    const beginStreamingAssistantMessage = (messageId, activeSessionId) => {
        resetPreviewPlayback();
        chatRun.value = startMessageRun(chatRun.value, {
            runId: messageId,
            messageId,
            sessionId: activeSessionId ?? sessionId.value,
            statusText: '正在思考...'
        });
    };

    const finishStreamingMessage = (completionPayload = {}) => {
        const streamingAssistantMessageId = getStreamingAssistantMessageId();
        const finalMessage = completionPayload?.finalMessage
            ? normalizeMessage(completionPayload.finalMessage)
            : null;
        if (!streamingAssistantMessageId) {
            chatRun.value = completeMessageRun(chatRun.value, {
                sessionId: completionPayload.sessionId ?? sessionId.value,
                finalMessage,
                statusText: '回复已完成'
            });
            clearStreamingState();
            return;
        }

        const messageIndex = findMessageIndex(streamingAssistantMessageId);
        if (messageIndex >= 0) {
            const current = messages.value[messageIndex];
            const normalized = resolveCompletedStreamingMessage(current, completionPayload);

            messages.value[messageIndex] = {
                ...current,
                ...normalized,
                streaming: false
            };
        }
        chatRun.value = completeMessageRun(chatRun.value, {
            sessionId: sessionId.value,
            finalMessage,
            statusText: '回复已完成'
        });
        clearStreamingState();
    };

    const resolveCompletedStreamingMessage = (current, completionPayload = {}) => {
        const previewText = typeof completionPayload?.previewText === 'string'
            ? completionPayload.previewText
            : getStreamingPreviewText();
        if (completionPayload?.finalMessage && typeof completionPayload.finalMessage === 'object') {
            const normalized = normalizeMessage({
                ...completionPayload.finalMessage,
                id: completionPayload.finalMessage.id ?? current.id,
                role: completionPayload.finalMessage.role ?? 'ASSISTANT'
            });
            const shouldUsePreviewFallback = normalized.messageType === AGENT_MESSAGE_TYPE.TEXT
                && (!normalized.content || completionPayload?.telemetry?.previewFallbackApplied);
            if (shouldUsePreviewFallback && previewText) {
                return {
                    ...normalized,
                    content: previewText
                };
            }
            return normalized;
        }
        return normalizeMessage({
            id: current.id,
            role: 'ASSISTANT',
            messageType: AGENT_MESSAGE_TYPE.TEXT,
            content: previewText || current.content
        });
    };

    const discardStreamingMessage = (errorMessage = '') => {
        const streamingAssistantMessageId = getStreamingAssistantMessageId();
        if (!streamingAssistantMessageId) {
            chatRun.value = failMessageRun(chatRun.value, errorMessage);
            clearStreamingState();
            return;
        }

        const messageIndex = findMessageIndex(streamingAssistantMessageId);
        if (messageIndex >= 0) {
            const current = messages.value[messageIndex];
            const previewText = getStreamingPreviewText();
            if (!current.content && !previewText) {
                messages.value.splice(messageIndex, 1);
            } else {
                messages.value[messageIndex] = {
                    ...current,
                    content: current.content || previewText,
                    streaming: false
                };
            }
        }
        chatRun.value = failMessageRun(chatRun.value, errorMessage);
        clearStreamingState();
    };

    // WebSocket setup
    const resolveAgentSocketUrl = () => {
        const wsUrl = formatWsUrl(API_CONFIG.baseURL);
        return `${wsUrl}/api/agent/ws`;
    };

    const connectWebSocket = () => {
        if (socketService) return socketService;

        closingSocket = false;
        socketService = new SocketService(resolveAgentSocketUrl(), getAccessToken());

        socketService.on(AGENT_RUNTIME_EVENT.MESSAGE_STARTED, (payload = {}) => {
            console.log('[Agent Studio] Message started:', payload);
            chatRun.value = startMessageRun(chatRun.value, {
                runId: chatRun.value.runId ?? getStreamingAssistantMessageId(),
                messageId: getStreamingAssistantMessageId(),
                sessionId: payload.sessionId ?? sessionId.value,
                statusText: payload.statusText || '正在思考...'
            });
        });

        socketService.on(AGENT_RUNTIME_EVENT.MESSAGE_DELTA, (payload = {}) => {
            const streamingAssistantMessageId = getStreamingAssistantMessageId();
            if (!streamingAssistantMessageId || typeof payload.delta !== 'string') return;
            const messageIndex = findMessageIndex(streamingAssistantMessageId);
            if (messageIndex < 0) return;

            console.log('[Agent Studio] Message delta received:', { 
                length: payload.delta.length, 
                preview: payload.delta.substring(0, 50),
                totalPreviewLength: chatRun.value.previewText?.length || 0,
                queuedTokens: queuedPreviewTokens.length
            });
            enqueuePreviewDelta(payload.delta);
        });

        socketService.on(AGENT_RUNTIME_EVENT.MESSAGE_COMPLETED, (payload = {}) => {
            console.log('[Agent Studio] Message completed:', {
                currentScene: payload.currentScene,
                nextScene: payload.nextScene,
                previewMode: payload.telemetry?.previewMode,
                deltaCount: payload.telemetry?.deltaCount,
                previewLength: payload.previewText?.length || 0
            });
            applyScenePayload(payload);
            if (hasPendingPreviewPlayback()) {
                pendingCompletionPayload = payload;
                schedulePreviewPlayback();
            } else {
                finishStreamingMessage(payload);
            }
            syncActiveSessionHistory({
                sceneType: payload.currentScene ?? currentScene.value,
                updateTime: new Date().toISOString()
            }, {
                removeWhenFiltered: true
            });
        });

        socketService.on(AGENT_RUNTIME_EVENT.MESSAGE_FAILED, (payload = {}) => {
            console.error('[Agent Studio] Message failed:', payload.message);
            discardStreamingMessage(payload.message || '发送失败,请稍后重试');
            ElMessage.error(payload.message || '发送失败,请稍后重试');
        });

        socketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_STARTED, (payload = {}) => {
            console.log('[Agent Studio] Artifact generation started:', payload);
            draftRun.value = startArtifactRun(draftRun.value, {
                runId: payload.sessionId ?? sessionId.value,
                sessionId: payload.sessionId ?? sessionId.value,
                source: 'agent-studio',
                statusText: payload.statusText || '正在整理会话上下文...'
            });
        });

        socketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_STATUS, (payload = {}) => {
            console.log('[Agent Studio] Artifact status update:', payload.statusText);
            draftRun.value = updateArtifactStatus(
                generatingDraft.value
                    ? draftRun.value
                    : startArtifactRun(draftRun.value, {
                        runId: payload.sessionId ?? sessionId.value,
                        sessionId: payload.sessionId ?? sessionId.value,
                        source: 'agent-studio'
                    }),
                payload.statusText || draftStreamingStatusText.value || '正在生成首稿内容...'
            );
        });

        socketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_DELTA, (payload = {}) => {
            if (typeof payload.chunk !== 'string' || !payload.chunk) {
                return;
            }
            console.log('[Agent Studio] Artifact delta received:', { 
                chunkLength: payload.chunk.length,
                preview: payload.chunk.substring(0, 50),
                totalBufferLength: draftRun.value.artifactBuffer?.length || 0
            });
            draftRun.value = appendArtifactDelta(draftRun.value, {
                chunk: payload.chunk,
                statusText: draftStreamingStatusText.value || '正在生成首稿内容...',
                buildPreview: buildStreamingDraftPreview
            });
        });

        socketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_COMPLETED, (payload = {}) => {
            console.log('[Agent Studio] Artifact generation completed:', { draftId: payload.finalArtifact?.draftId, versionNo: payload.finalArtifact?.versionNo });
            draft.value = normalizeAgentDraft(payload.finalArtifact || {});
            candidateDraft.value = null;
            draftRun.value = completeArtifactRun(draftRun.value, {
                sessionId: payload.sessionId ?? sessionId.value,
                artifactPreview: draftRun.value.artifactPreview,
                finalArtifact: draft.value,
                statusText: '首稿已生成'
            });
            resetDraftStreamingState({ resetRuntime: false });
        
            if (session.value) {
                session.value.targetDraftId = draft.value?.draftId ?? null;
            }
            applyScenePayload(payload, notebook.value ? {
                ...notebook.value,
                currentScene: payload.currentScene ?? AGENT_SCENE_TYPE.DRAFT,
                draftReadiness: payload.draftReadiness ?? AGENT_DRAFT_READINESS.READY,
                nextSuggestedAction: payload.assistantAction ?? AGENT_ASSISTANT_ACTION.EDIT_DRAFT
            } : {
                currentScene: payload.currentScene ?? AGENT_SCENE_TYPE.DRAFT,
                draftReadiness: payload.draftReadiness ?? AGENT_DRAFT_READINESS.READY,
                nextSuggestedAction: payload.assistantAction ?? AGENT_ASSISTANT_ACTION.EDIT_DRAFT
            });
        
            messages.value.push(normalizeMessage({
                id: `system-${Date.now()}`,
                role: 'SYSTEM',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '✅ 首稿生成完毕。你可以继续在对话框中圈出需要修改的段落或者补充新要求。'
            }));
        
            syncActiveSessionHistory({
                sceneType: payload.currentScene ?? AGENT_SCENE_TYPE.DRAFT,
                targetDraftId: draft.value?.draftId ?? null,
                updateTime: new Date().toISOString()
            }, {
                removeWhenFiltered: true
            });
        
            ElMessage.success('首稿已生成,可以继续优化或导入编辑器');
        });

        socketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_FAILED, (payload = {}) => {
            console.error('[Agent Studio] Artifact generation failed:', payload.message);
            draftRun.value = failArtifactRun(draftRun.value, payload.message || '生成草稿失败,请稍后重试');
            resetDraftStreamingState({ resetRuntime: false });
            ElMessage.error(payload.message || '生成草稿失败,请稍后重试');
        });

        socketService.onClose(() => {
            console.log('[Agent Studio] WebSocket connection closed');
            if (closingSocket) return;
            if (chatting.value) {
                discardStreamingMessage();
                ElMessage.error('Agent 连接已断开,请重试');
            }
            if (generatingDraft.value) {
                draftRun.value = failArtifactRun(draftRun.value, 'Agent 连接已断开,首稿生成已中断');
                resetDraftStreamingState({ resetRuntime: false });
                ElMessage.error('Agent 连接已断开,首稿生成已中断');
            }
        });

        socketService.onError((error) => {
            console.error('Agent WebSocket error:', error);
        });

        socketService.connect();
        return socketService;
    };

    const ensureSocketReady = async (timeoutMs = 5000) => {
        const service = connectWebSocket();
        if (service.isConnected()) return service;
        if (socketReadyPromise) return socketReadyPromise;

        socketReadyPromise = new Promise((resolve, reject) => {
            const startedAt = Date.now();
            const timer = setInterval(() => {
                if (service.isConnected()) {
                    clearInterval(timer);
                    socketReadyPromise = null;
                    resolve(service);
                    return;
                }

                const readyState = service?.socket?.readyState;
                if (readyState === WebSocket.CLOSING || readyState === WebSocket.CLOSED) {
                    clearInterval(timer);
                    socketReadyPromise = null;
                    reject(new Error('Agent WebSocket handshake failed'));
                    return;
                }

                if (Date.now() - startedAt >= timeoutMs) {
                    clearInterval(timer);
                    socketReadyPromise = null;
                    reject(new Error('Agent WebSocket connect timeout'));
                }
            }, 100);
        });
        return socketReadyPromise;
    };

    const disconnectWebSocket = () => {
        closingSocket = true;
        socketReadyPromise = null;
        clearStreamingState({ resetRuntime: true });
        if (socketService) {
            socketService.close();
            socketService = null;
        }
        resetDraftStreamingState();
    };

    // Actions
    const resetStudioState = () => {
        session.value = null;
        sessionId.value = null;
        sessionTitle.value = DEFAULT_SESSION_TITLE;
        messages.value = [];
        notebook.value = null;
        currentScene.value = AGENT_SCENE_TYPE.DISCUSS;
        nextScene.value = AGENT_SCENE_TYPE.DISCUSS;
        draftReadiness.value = AGENT_DRAFT_READINESS.NOT_READY;
        assistantAction.value = AGENT_ASSISTANT_ACTION.ASK;
        switchReason.value = '';
        draft.value = null;
        candidateDraft.value = null;
        clearStreamingState({ resetRuntime: true });
        resetDraftStreamingState();
    };

    const fetchSessionHistory = async ({ reset = false, keyword = sessionHistoryKeyword.value } = {}) => {
        if (!reset && (!hasMoreSessionHistory.value || sessionHistoryLoading.value || sessionHistoryLoadingMore.value)) {
            return;
        }

        const normalizedKeyword = String(keyword || '').trim();
        const nextPageNo = reset ? 1 : sessionHistoryPageNo.value + 1;
        const requestSerial = ++sessionHistoryRequestSerial;

        if (reset) {
            sessionHistoryLoadingMore.value = false;
            sessionHistoryLoading.value = true;
            sessionHistoryKeyword.value = normalizedKeyword;
        } else {
            sessionHistoryLoadingMore.value = true;
        }

        try {
            const response = await getAgentSessionPage(nextPageNo, sessionHistoryPageSize.value, normalizedKeyword);
            if (requestSerial !== sessionHistoryRequestSerial || response === null) {
                return;
            }

            const nextRecords = Array.isArray(response?.list)
                ? response.list.map(normalizeSessionSummary)
                : [];

            sessionHistory.value = reset
                ? sortSessionHistory(nextRecords)
                : mergeSessionHistory(sessionHistory.value, nextRecords);
            sessionHistoryTotal.value = Number(response?.total ?? sessionHistory.value.length);
            sessionHistoryPageNo.value = nextPageNo;
        } catch (error) {
            console.error('Failed to fetch agent session history:', error);
            ElMessage.error('查询历史会话失败，请稍后重试');
        } finally {
            if (requestSerial === sessionHistoryRequestSerial) {
                if (reset) {
                    sessionHistoryLoading.value = false;
                } else {
                    sessionHistoryLoadingMore.value = false;
                }
            }
        }
    };

    const refreshSessionHistory = async (keyword = sessionHistoryKeyword.value) => {
        sessionHistoryPageNo.value = 0;
        sessionHistoryTotal.value = 0;
        await fetchSessionHistory({
            reset: true,
            keyword
        });
    };

    const loadMoreSessionHistory = async () => {
        await fetchSessionHistory();
    };

    const openSessionHistory = async (target) => {
        const targetSessionId = Number(target?.id ?? target);
        if (!targetSessionId || targetSessionId === sessionId.value) {
            return;
        }
        if (chatting.value || generatingDraft.value) {
            ElMessage.warning('当前流程尚未完成，请稍后再切换会话');
            return;
        }
        await router.push({
            name: 'AgentStudio',
            params: { sessionId: targetSessionId }
        });
    };

    const hydrateSession = async (id) => {
        if (!id) {
            resetStudioState();
            return;
        }

        loadingSession.value = true;
        try {
            const detail = await getAgentSessionDetail(id);
            session.value = detail.session || null;
            sessionId.value = detail.session?.id ?? id;
            sessionTitle.value = detail.session?.title || DEFAULT_SESSION_TITLE;
            messages.value = Array.isArray(detail.messages)
                ? detail.messages.map(normalizeMessage)
                : [];
            notebook.value = detail.notebook ? normalizeNotebook(detail.notebook) : null;
            draft.value = detail.draft ? normalizeAgentDraft(detail.draft) : null;
            candidateDraft.value = null;
            clearStreamingState({ resetRuntime: true });
            resetDraftStreamingState();
            upsertSessionHistory(detail.session || {}, {
                removeWhenFiltered: true
            });
            applyScenePayload({
                currentScene: detail.notebook?.currentScene ?? detail.session?.sceneType,
                nextScene: detail.notebook?.currentScene ?? detail.session?.sceneType,
                draftReadiness: detail.notebook?.draftReadiness,
                assistantAction: detail.notebook?.nextSuggestedAction
            }, detail.notebook ?? null);
        } catch (error) {
            console.error('Failed to hydrate agent session:', error);
            ElMessage.error('恢复会话失败，请稍后重试');
        } finally {
            loadingSession.value = false;
        }
    };

    const ensureSession = async (seedMessage = '') => {
        if (sessionId.value) return sessionId.value;

        creatingSession.value = true;
        try {
            const title = sessionTitle.value?.trim() || seedMessage.trim().slice(0, 20) || DEFAULT_SESSION_TITLE;
            const response = await createAgentSession({
                sceneType: AGENT_SCENE_TYPE.DISCUSS,
                title
            });
            const now = new Date().toISOString();
            sessionId.value = response.sessionId;
            session.value = {
                id: response.sessionId,
                title,
                sceneType: AGENT_SCENE_TYPE.DISCUSS,
                status: 'ACTIVE',
                targetDraftId: null,
                createTime: now,
                updateTime: now
            };
            sessionTitle.value = title;
            notebook.value = normalizeNotebook({
                goal: title,
                currentScene: AGENT_SCENE_TYPE.DISCUSS,
                draftReadiness: AGENT_DRAFT_READINESS.NOT_READY,
                nextSuggestedAction: AGENT_ASSISTANT_ACTION.ASK
            });
            applyScenePayload({
                currentScene: AGENT_SCENE_TYPE.DISCUSS,
                nextScene: AGENT_SCENE_TYPE.DISCUSS,
                draftReadiness: AGENT_DRAFT_READINESS.NOT_READY,
                assistantAction: AGENT_ASSISTANT_ACTION.ASK,
                switchReason: ''
            }, notebook.value);
            syncActiveSessionHistory({
                title,
                sceneType: AGENT_SCENE_TYPE.DISCUSS,
                createTime: now,
                updateTime: now
            }, {
                removeWhenFiltered: true
            });
            await router.replace({
                name: 'AgentStudio',
                params: { sessionId: response.sessionId }
            });
            return response.sessionId;
        } finally {
            creatingSession.value = false;
        }
    };

    const openFreshSession = async () => {
        if (chatting.value || generatingDraft.value) {
            ElMessage.warning('当前生成流程尚未完成，请稍后再新建会话');
            return;
        }
        resetStudioState();
        sessionTitle.value = DEFAULT_SESSION_TITLE;
        await router.push({ name: 'AgentStudio' });
    };

    const sendMessage = async (content) => {
        if (!content) return;
        if (hasPendingInteractiveForm.value) {
            ElMessage.warning('请先完成当前问题卡片');
            return;
        }
    
        console.log('[Agent Studio] Sending message:', { sessionId: sessionId.value, contentLength: content.length });
        try {
            const currentSessionId = await ensureSession(content);
            const service = await ensureSocketReady();
    
            messages.value.push(normalizeMessage({
                id: `user-${Date.now()}`,
                role: 'USER',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content
            }));
            syncActiveSessionHistory({
                updateTime: new Date().toISOString()
            }, {
                removeWhenFiltered: true
            });
    
            const assistantMessageId = `assistant-${Date.now()}`;
            beginStreamingAssistantMessage(assistantMessageId, currentSessionId);
            messages.value.push(normalizeMessage({
                id: assistantMessageId,
                role: 'ASSISTANT',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '',
                streaming: true
            }));
    
            const sent = service.send(AGENT_RUNTIME_COMMAND.MESSAGE_CREATE, {
                sessionId: currentSessionId,
                content
            });
            console.log('[Agent Studio] Message sent successfully:', sent);
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('[Agent Studio] Failed to send agent message:', error);
            discardStreamingMessage();
            ElMessage.error('发送失败,请稍后重试');
        }
    };

    const createDraftFromSession = async () => {
        if (!sessionId.value) {
            ElMessage.warning('请先开始一段主题讨论');
            return;
        }
        if (hasPendingInteractiveForm.value) {
            ElMessage.warning('请先完成当前问题卡片，再生成初稿');
            return;
        }
        if (chatting.value) {
            ElMessage.warning('当前回复尚未完成，请稍后再生成初稿');
            return;
        }
        if (!isDraftReady.value && currentScene.value !== AGENT_SCENE_TYPE.EDIT) {
            ElMessage.warning('当前材料还没有准备到首稿阶段，建议先继续讨论或学习补齐关键信息');
            return;
        }

        draftRun.value = startArtifactRun(draftRun.value, {
            runId: sessionId.value,
            sessionId: sessionId.value,
            source: 'agent-studio',
            statusText: '正在跳转编辑器'
        });
        try {
            saveAgentGenerationIntent({
                sessionId: sessionId.value,
                source: 'agent-studio'
            });
            await router.push('/text');
        } catch (error) {
            console.error('Failed to open editor for agent draft generation:', error);
            draftRun.value = failArtifactRun(draftRun.value, '打开编辑器失败，请稍后重试');
            resetDraftStreamingState({ resetRuntime: false });
            ElMessage.error('打开编辑器失败，请稍后重试');
        }
    };

    const optimizeCurrentDraft = async (instruction) => {
        if (!draft.value?.draftId) {
            ElMessage.warning('请先生成首稿');
            return;
        }
        if (!instruction || !instruction.trim()) return;

        optimizingDraft.value = true;
        try {
            const response = await optimizeAgentDraft({
                draftId: draft.value.draftId,
                instruction: instruction.trim()
            });
            candidateDraft.value = normalizeAgentDraft({
                draftId: response.draftId,
                versionNo: response.candidateVersionNo,
                title: response.title,
                summary: response.summary,
                content: response.content
            });
            
            // Log as user instruction
            messages.value.push(normalizeMessage({
                id: `user-opt-${Date.now()}`,
                role: 'USER',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: `(局部优化指令): ${instruction.trim()}`
            }));
            // Log as system acknowledgement
            messages.value.push(normalizeMessage({
                id: `system-opt-${Date.now()}`,
                role: 'SYSTEM',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '✅ 候选版本已生成，正显示在画布中。您可以点击画布顶部的确认或撤销按钮。'
            }));
            
            ElMessage.success('候选版本已生成');
        } catch (error) {
            console.error('Failed to optimize agent draft:', error);
            ElMessage.error('生成候选版本失败，请稍后重试');
        } finally {
            optimizingDraft.value = false;
        }
    };

    const adoptCandidateVersion = async () => {
        if (!draft.value?.draftId || !candidateDraft.value?.versionNo) {
            ElMessage.warning('当前没有候选草稿可应用');
            return;
        }
        adoptingCandidate.value = true;
        try {
            await adoptAgentDraftVersion({
                draftId: draft.value.draftId,
                versionNo: candidateDraft.value.versionNo
            });
            draft.value = { ...candidateDraft.value };
            candidateDraft.value = null;
            ElMessage.success('已应用新版本');
        } catch (error) {
            console.error('Failed to adopt candidate version:', error);
            ElMessage.error('应用候选版本失败，请稍后重试');
        } finally {
            adoptingCandidate.value = false;
        }
    };
    
    const rejectCandidateVersion = () => {
        candidateDraft.value = null; // Reverts UI to active draft
        ElMessage.info('已撤销候选版本');
    };

    const importDraftToEditor = async () => {
        const finalDraft = candidateDraft.value || draft.value;
        if (!finalDraft?.content) {
            ElMessage.warning('请先生成一个草稿版本');
            return;
        }
        saveAgentDraftImport(finalDraft);
        await router.push('/text');
    };

    const submitInteractiveForm = async (message, answersMap = {}) => {
        if (!message?.payload?.formId) {
            ElMessage.error('当前问题卡片数据无效');
            return;
        }
        if (chatting.value) {
            ElMessage.warning('当前回复尚未完成,请稍后重试');
            return;
        }
    
        const interactionResponse = buildInteractionResponsePayload(message.payload, answersMap);
        if (!interactionResponse?.answers?.length) {
            ElMessage.warning('请至少完成一个问题');
            return;
        }
    
        console.log('[Agent Studio] Submitting interactive form:', { formId: message.payload.formId, answersCount: interactionResponse.answers.length });
        const optimisticMessageId = `user-form-${Date.now()}`;
        try {
            const service = await ensureSocketReady();
    
            messages.value.push(normalizeMessage({
                id: optimisticMessageId,
                role: 'USER',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: buildInteractionResponseSummary(message.payload, answersMap),
                payload: { interactionResponse }
            }));
            syncActiveSessionHistory({
                updateTime: new Date().toISOString()
            }, {
                removeWhenFiltered: true
            });
    
            const assistantMessageId = `assistant-${Date.now()}`;
            beginStreamingAssistantMessage(assistantMessageId, sessionId.value);
            messages.value.push(normalizeMessage({
                id: assistantMessageId,
                role: 'ASSISTANT',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '',
                streaming: true
            }));
    
            const sent = service.send(AGENT_RUNTIME_COMMAND.MESSAGE_CREATE, {
                sessionId: sessionId.value,
                interactionResponse
            });
            console.log('[Agent Studio] Interactive form submitted:', sent);
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('[Agent Studio] Failed to submit interactive form:', error);
            const optimisticIndex = findMessageIndex(optimisticMessageId);
            if (optimisticIndex >= 0) {
                messages.value.splice(optimisticIndex, 1);
            }
            discardStreamingMessage();
            ElMessage.error('提交失败,请稍后重试');
        }
    };

    return {
        // State
        loadingSession,
        creatingSession,
        chatting,
        generatingDraft,
        optimizingDraft,
        adoptingCandidate,
        session,
        sessionId,
        sessionTitle,
        messages,
        notebook,
        currentScene,
        nextScene,
        draftReadiness,
        assistantAction,
        switchReason,
        sessionHistory,
        sessionHistoryLoading,
        sessionHistoryLoadingMore,
        sessionHistoryTotal,
        sessionHistoryKeyword,
        visibleMessages,
        draft,
        candidateDraft,
        streamingDraftPreview,
        draftStreamingStatusText,
        
        // Computed
        hasMessages,
        hasDraft,
        hasCandidateDraft,
        isDraftReady,
        hasPendingInteractiveForm,
        draftStatus,
        displayDraft,
        activeDraftVersion,
        pendingDraftVersion,
        sessionStatusLabel,
        currentSceneLabel,
        nextSceneLabel,
        currentSceneSubtitle,
        sceneFooterHint,
        generateButtonLabel,
        hasSessionHistory,
        hasMoreSessionHistory,
        
        // Methods
        connectWebSocket,
        disconnectWebSocket,
        resetStudioState,
        fetchSessionHistory,
        refreshSessionHistory,
        loadMoreSessionHistory,
        openSessionHistory,
        hydrateSession,
        ensureSession,
        openFreshSession,
        sendMessage,
        submitInteractiveForm,
        createDraftFromSession,
        optimizeCurrentDraft,
        adoptCandidateVersion,
        rejectCandidateVersion,
        importDraftToEditor
    };
});
