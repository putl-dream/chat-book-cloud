import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { ElMessage } from 'element-plus';

import { API_CONFIG } from '@/config/index.js';
import { getAccessToken } from '@/utils/token.js';
import SocketService, { formatWsUrl } from '@/utils/websocket.js';
import {
    AGENT_SCENE_TYPE,
    adoptAgentDraftVersion,
    createAgentSession,
    getAgentSessionDetail,
    normalizeAgentDraft,
    optimizeAgentDraft,
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
import router from '@/router/index.js';

const DEFAULT_SESSION_TITLE = '新的 AI 创作会话';
const AGENT_CHAT_TYPE = 'AGENT_CHAT';
const AGENT_CHAT_DELTA = 'AGENT_CHAT_DELTA';
const AGENT_CHAT_DONE = 'AGENT_CHAT_DONE';
const AGENT_CHAT_ERROR = 'AGENT_CHAT_ERROR';
const AGENT_DRAFT_GENERATE = 'AGENT_DRAFT_GENERATE';
const AGENT_DRAFT_GENERATE_START = 'AGENT_DRAFT_GENERATE_START';
const AGENT_DRAFT_GENERATE_STATUS = 'AGENT_DRAFT_GENERATE_STATUS';
const AGENT_DRAFT_GENERATE_DELTA = 'AGENT_DRAFT_GENERATE_DELTA';
const AGENT_DRAFT_GENERATE_DONE = 'AGENT_DRAFT_GENERATE_DONE';
const AGENT_DRAFT_GENERATE_ERROR = 'AGENT_DRAFT_GENERATE_ERROR';

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

function decodeJsonStringFragment(value = '') {
    let candidate = value;
    while (candidate.length > 0) {
        try {
            return JSON.parse(`"${candidate}"`);
        } catch (error) {
            candidate = candidate.slice(0, -1);
        }
    }
    return '';
}

function extractDraftField(buffer = '', fieldName) {
    const matcher = new RegExp(`"${fieldName}"\\s*:\\s*"`, 'm');
    const match = matcher.exec(buffer);
    if (!match) {
        return '';
    }

    let rawValue = '';
    let consecutiveBackslashes = 0;
    for (let cursor = match.index + match[0].length; cursor < buffer.length; cursor += 1) {
        const char = buffer[cursor];
        if (char === '"' && consecutiveBackslashes % 2 === 0) {
            return decodeJsonStringFragment(rawValue);
        }
        rawValue += char;
        consecutiveBackslashes = char === '\\' ? consecutiveBackslashes + 1 : 0;
    }
    return decodeJsonStringFragment(rawValue);
}

function buildStreamingDraftPreview(buffer = '') {
    const title = extractDraftField(buffer, 'title');
    const summary = extractDraftField(buffer, 'summary');
    const content = extractDraftField(buffer, 'content');
    if (!title && !summary && !content) {
        return null;
    }
    return normalizeAgentDraft({ title, summary, content });
}

export const useAgentStudioStore = defineStore('agentStudio', () => {
    // Basic States
    const loadingSession = ref(false);
    const creatingSession = ref(false);
    const chatting = ref(false);
    const generatingDraft = ref(false);
    const optimizingDraft = ref(false);
    const adoptingCandidate = ref(false);

    // Context
    const session = ref(null);
    const sessionId = ref(null);
    const sessionTitle = ref(DEFAULT_SESSION_TITLE);
    const messages = ref([]);
    
    // Draft states
    const draft = ref(null);
    const candidateDraft = ref(null);
    const draftStreamingBuffer = ref('');
    const draftStreamingPreview = ref(null);
    const draftStreamingStatusText = ref('');

    // WebSocket Internals (not reactive)
    let socketService = null;
    let socketReadyPromise = null;
    let streamingAssistantMessageId = null;
    let closingSocket = false;

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
            if (message.role === 'assistant' && message.messageType === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM) {
                const formId = message.payload?.formId;
                return {
                    ...message,
                    interactionResponse: formId ? (interactionResponseMap.value.get(formId) ?? null) : null
                };
            }
            return message;
        }));

    const hasMessages = computed(() => visibleMessages.value.length > 0);
    const hasDraft = computed(() => Boolean(draft.value?.draftId));
    const hasCandidateDraft = computed(() => Boolean(candidateDraft.value?.versionNo));
    const hasPendingInteractiveForm = computed(() => visibleMessages.value.some((message) =>
        message.role === 'assistant'
        && message.messageType === AGENT_MESSAGE_TYPE.INTERACTIVE_FORM
        && !message.interactionResponse
    ));
    
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
        return draftStreamingPreview.value;
    });

    const activeDraftVersion = computed(() => draft.value?.versionNo ?? null);
    const pendingDraftVersion = computed(() => candidateDraft.value?.versionNo ?? null);

    const sessionStatusLabel = computed(() => {
        if (loadingSession.value) return '正在恢复';
        if (chatting.value) return '对话进行中';
        if (generatingDraft.value) return '生成首稿中';
        if (optimizingDraft.value) return '优化重写中';
        if (sessionId.value) return '会话已激活';
        return '未创建会话';
    });

    // Message Utilities
    const findMessageIndex = (messageId) => messages.value.findIndex((item) => item.id === messageId);

    const clearStreamingState = () => {
        streamingAssistantMessageId = null;
        chatting.value = false;
    };

    const resetDraftStreamingState = () => {
        draftStreamingBuffer.value = '';
        draftStreamingPreview.value = null;
        draftStreamingStatusText.value = '';
    };

    const finishStreamingMessage = (reply = '') => {
        if (!streamingAssistantMessageId) {
            chatting.value = false;
            return;
        }

        const messageIndex = findMessageIndex(streamingAssistantMessageId);
        if (messageIndex >= 0) {
            const current = messages.value[messageIndex];
            const normalized = typeof reply === 'object' && reply !== null
                ? normalizeMessage({
                    ...reply,
                    id: reply.id ?? current.id,
                    role: reply.role ?? 'ASSISTANT'
                })
                : normalizeMessage({
                    id: current.id,
                    role: 'ASSISTANT',
                    messageType: AGENT_MESSAGE_TYPE.TEXT,
                    content: typeof reply === 'string' && reply.length > 0 ? reply : current.content
                });
            messages.value[messageIndex] = {
                ...current,
                ...normalized,
                streaming: false
            };
        }
        clearStreamingState();
    };

    const discardStreamingMessage = () => {
        if (!streamingAssistantMessageId) {
            chatting.value = false;
            return;
        }

        const messageIndex = findMessageIndex(streamingAssistantMessageId);
        if (messageIndex >= 0) {
            const current = messages.value[messageIndex];
            if (!current.content) {
                messages.value.splice(messageIndex, 1);
            } else {
                messages.value[messageIndex] = {
                    ...current,
                    streaming: false
                };
            }
        }
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

        socketService.on(AGENT_CHAT_DELTA, (payload = {}) => {
            if (!streamingAssistantMessageId || typeof payload.content !== 'string') return;
            const messageIndex = findMessageIndex(streamingAssistantMessageId);
            if (messageIndex < 0) return;

            const current = messages.value[messageIndex];
            messages.value[messageIndex] = {
                ...current,
                content: `${current.content || ''}${payload.content}`,
                streaming: true
            };
        });

        socketService.on(AGENT_CHAT_DONE, (payload = {}) => {
            finishStreamingMessage(payload.message ?? payload.reply ?? '');
        });

        socketService.on(AGENT_CHAT_ERROR, (payload = {}) => {
            discardStreamingMessage();
            ElMessage.error(payload.message || '发送失败，请稍后重试');
        });

        socketService.on(AGENT_DRAFT_GENERATE_START, (payload = {}) => {
            generatingDraft.value = true;
            draftStreamingBuffer.value = '';
            draftStreamingPreview.value = null;
            draftStreamingStatusText.value = payload.message || '正在整理会话上下文...';
        });

        socketService.on(AGENT_DRAFT_GENERATE_STATUS, (payload = {}) => {
            if (!generatingDraft.value) {
                generatingDraft.value = true;
            }
            draftStreamingStatusText.value = payload.message || draftStreamingStatusText.value || '正在生成首稿内容...';
        });

        socketService.on(AGENT_DRAFT_GENERATE_DELTA, (payload = {}) => {
            if (typeof payload.chunk !== 'string' || !payload.chunk) {
                return;
            }
            draftStreamingBuffer.value = `${draftStreamingBuffer.value}${payload.chunk}`;
            draftStreamingStatusText.value = draftStreamingStatusText.value || '正在生成首稿内容...';
            draftStreamingPreview.value = buildStreamingDraftPreview(draftStreamingBuffer.value);
        });

        socketService.on(AGENT_DRAFT_GENERATE_DONE, (payload = {}) => {
            draft.value = normalizeAgentDraft({
                draftId: payload.draftId,
                versionNo: payload.versionNo,
                title: payload.title,
                summary: payload.summary,
                content: payload.content
            });
            candidateDraft.value = null;
            generatingDraft.value = false;
            resetDraftStreamingState();

            if (session.value) {
                session.value.targetDraftId = payload.draftId;
            }

            messages.value.push(normalizeMessage({
                id: `system-${Date.now()}`,
                role: 'SYSTEM',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '✅ 首稿生成完毕。你可以继续在对话框中圈出需要修改的段落或者补充新要求。'
            }));

            ElMessage.success('首稿已生成，可以继续优化或导入编辑器');
        });

        socketService.on(AGENT_DRAFT_GENERATE_ERROR, (payload = {}) => {
            generatingDraft.value = false;
            resetDraftStreamingState();
            ElMessage.error(payload.message || '生成草稿失败，请稍后重试');
        });

        socketService.onClose(() => {
            if (closingSocket) return;
            if (chatting.value) {
                discardStreamingMessage();
                ElMessage.error('Agent 连接已断开，请重试');
            }
            if (generatingDraft.value) {
                generatingDraft.value = false;
                resetDraftStreamingState();
                ElMessage.error('Agent 连接已断开，首稿生成已中断');
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
        streamingAssistantMessageId = null;
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
        messages.value = [];
        draft.value = null;
        candidateDraft.value = null;
        streamingAssistantMessageId = null;
        resetDraftStreamingState();
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
            draft.value = detail.draft ? normalizeAgentDraft(detail.draft) : null;
            candidateDraft.value = null;
            streamingAssistantMessageId = null;
            resetDraftStreamingState();
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
                sceneType: AGENT_SCENE_TYPE.CREATE,
                title
            });
            sessionId.value = response.sessionId;
            session.value = {
                id: response.sessionId,
                title,
                sceneType: AGENT_SCENE_TYPE.CREATE,
                status: 'ACTIVE',
                targetDraftId: null
            };
            sessionTitle.value = title;
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

        chatting.value = true;
        try {
            const currentSessionId = await ensureSession(content);
            const service = await ensureSocketReady();

            messages.value.push(normalizeMessage({
                id: `user-${Date.now()}`,
                role: 'USER',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content
            }));

            const assistantMessageId = `assistant-${Date.now()}`;
            streamingAssistantMessageId = assistantMessageId;
            messages.value.push(normalizeMessage({
                id: assistantMessageId,
                role: 'ASSISTANT',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '',
                streaming: true
            }));

            const sent = service.send(AGENT_CHAT_TYPE, {
                sessionId: currentSessionId,
                content
            });
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('Failed to send agent message:', error);
            discardStreamingMessage();
            ElMessage.error('发送失败，请稍后重试');
        }
    };

    const createDraftFromSession = async () => {
        if (!sessionId.value) {
            ElMessage.warning('请先开始一段创作对话');
            return;
        }
        if (hasPendingInteractiveForm.value) {
            ElMessage.warning('请先完成当前问题卡片，再生成首稿');
            return;
        }
        if (chatting.value) {
            ElMessage.warning('当前回复尚未完成，请稍后再生成首稿');
            return;
        }

        generatingDraft.value = true;
        try {
            resetDraftStreamingState();
            draftStreamingStatusText.value = '正在连接生成通道...';
            const service = await ensureSocketReady();
            const sent = service.send(AGENT_DRAFT_GENERATE, {
                sessionId: sessionId.value
            });
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('Failed to generate agent draft:', error);
            generatingDraft.value = false;
            resetDraftStreamingState();
            ElMessage.error('生成草稿失败，请稍后重试');
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
            ElMessage.warning('当前回复尚未完成，请稍后重试');
            return;
        }

        const interactionResponse = buildInteractionResponsePayload(message.payload, answersMap);
        if (!interactionResponse?.answers?.length) {
            ElMessage.warning('请至少完成一个问题');
            return;
        }

        chatting.value = true;
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

            const assistantMessageId = `assistant-${Date.now()}`;
            streamingAssistantMessageId = assistantMessageId;
            messages.value.push(normalizeMessage({
                id: assistantMessageId,
                role: 'ASSISTANT',
                messageType: AGENT_MESSAGE_TYPE.TEXT,
                content: '',
                streaming: true
            }));

            const sent = service.send(AGENT_CHAT_TYPE, {
                sessionId: sessionId.value,
                interactionResponse
            });
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('Failed to submit interactive form:', error);
            const optimisticIndex = findMessageIndex(optimisticMessageId);
            if (optimisticIndex >= 0) {
                messages.value.splice(optimisticIndex, 1);
            }
            discardStreamingMessage();
            ElMessage.error('提交失败，请稍后重试');
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
        visibleMessages,
        draft,
        candidateDraft,
        streamingDraftPreview,
        draftStreamingStatusText,
        
        // Computed
        hasMessages,
        hasDraft,
        hasCandidateDraft,
        hasPendingInteractiveForm,
        draftStatus,
        displayDraft,
        activeDraftVersion,
        pendingDraftVersion,
        sessionStatusLabel,
        
        // Methods
        connectWebSocket,
        disconnectWebSocket,
        resetStudioState,
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
