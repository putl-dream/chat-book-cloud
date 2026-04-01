import { computed, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';

import {
    AGENT_SCENE_TYPE,
    adoptAgentDraftVersion,
    chatWithAgent,
    createAgentSession,
    generateAgentDraft,
    getAgentSessionDetail,
    normalizeAgentDraft,
    optimizeAgentDraft,
    saveAgentDraftImport
} from '../_domain/agent.js';

const DEFAULT_SESSION_TITLE = '新的 AI 创作会话';
const DEFAULT_OPTIMIZE_INSTRUCTION = '请强化结构层次、表达节奏和可读性，保留原有事实与结论。';

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
        content: message.content ?? '',
        createTime: message.createTime ?? ''
    };
}

export function useAgentStudioLogic() {
    const route = useRoute();
    const router = useRouter();

    const loadingSession = ref(false);
    const creatingSession = ref(false);
    const chatting = ref(false);
    const generatingDraft = ref(false);
    const optimizingDraft = ref(false);
    const adoptingCandidate = ref(false);

    const session = ref(null);
    const sessionId = ref(null);
    const sessionTitle = ref(DEFAULT_SESSION_TITLE);
    const chatInput = ref('');
    const optimizeInstruction = ref(DEFAULT_OPTIMIZE_INSTRUCTION);
    const messages = ref([]);
    const draft = ref(null);
    const candidateDraft = ref(null);

    const hasMessages = computed(() => messages.value.length > 0);
    const hasDraft = computed(() => Boolean(draft.value?.draftId));
    const hasCandidateDraft = computed(() => Boolean(candidateDraft.value?.versionNo));
    const activeDraftVersion = computed(() => draft.value?.versionNo ?? null);
    const pendingDraftVersion = computed(() => candidateDraft.value?.versionNo ?? null);
    const draftCompareChips = computed(() => {
        if (!draft.value || !candidateDraft.value) {
            return [];
        }

        return [
            {
                key: 'title',
                label: '标题',
                changed: draft.value.title !== candidateDraft.value.title
            },
            {
                key: 'summary',
                label: '摘要',
                changed: draft.value.summary !== candidateDraft.value.summary
            },
            {
                key: 'content',
                label: '正文',
                changed: draft.value.content !== candidateDraft.value.content
            }
        ];
    });

    const sessionStatusLabel = computed(() => {
        if (loadingSession.value) {
            return '正在恢复';
        }
        if (chatting.value) {
            return '对话进行中';
        }
        if (generatingDraft.value) {
            return '生成草稿中';
        }
        if (optimizingDraft.value) {
            return '优化候选中';
        }
        if (sessionId.value) {
            return '会话已激活';
        }
        return '未创建会话';
    });

    const resetStudioState = () => {
        session.value = null;
        sessionId.value = null;
        messages.value = [];
        draft.value = null;
        candidateDraft.value = null;
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
        } catch (error) {
            console.error('Failed to hydrate agent session:', error);
            ElMessage.error('恢复会话失败，请稍后重试');
        } finally {
            loadingSession.value = false;
        }
    };

    const syncRouteSession = async (nextSessionId) => {
        const parsedId = nextSessionId ? Number(nextSessionId) : null;
        if (!parsedId) {
            resetStudioState();
            sessionTitle.value = DEFAULT_SESSION_TITLE;
            return;
        }

        if (sessionId.value === parsedId && session.value) {
            return;
        }

        await hydrateSession(parsedId);
    };

    watch(
        () => route.params.sessionId,
        (nextSessionId) => {
            syncRouteSession(nextSessionId);
        },
        { immediate: true }
    );

    const ensureSession = async (seedMessage = '') => {
        if (sessionId.value) {
            return sessionId.value;
        }

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
        resetStudioState();
        sessionTitle.value = DEFAULT_SESSION_TITLE;
        chatInput.value = '';
        optimizeInstruction.value = DEFAULT_OPTIMIZE_INSTRUCTION;
        await router.push({ name: 'AgentStudio' });
    };

    const sendMessage = async () => {
        const content = chatInput.value.trim();
        if (!content) {
            return;
        }

        chatting.value = true;
        try {
            const currentSessionId = await ensureSession(content);
            messages.value.push(normalizeMessage({
                id: `user-${Date.now()}`,
                role: 'USER',
                content
            }));

            chatInput.value = '';
            const response = await chatWithAgent({
                sessionId: currentSessionId,
                content
            });

            messages.value.push(normalizeMessage({
                id: `assistant-${Date.now()}`,
                role: 'ASSISTANT',
                content: response.reply
            }));
        } catch (error) {
            console.error('Failed to send agent message:', error);
            ElMessage.error('发送失败，请稍后重试');
        } finally {
            chatting.value = false;
        }
    };

    const createDraftFromSession = async () => {
        if (!sessionId.value) {
            ElMessage.warning('请先开始一段创作对话');
            return;
        }

        generatingDraft.value = true;
        try {
            const response = await generateAgentDraft({
                sessionId: sessionId.value
            });
            draft.value = normalizeAgentDraft({
                draftId: response.draftId,
                versionNo: response.versionNo,
                title: response.title,
                summary: response.summary,
                content: response.content
            });
            candidateDraft.value = null;

            if (session.value) {
                session.value.targetDraftId = response.draftId;
            }

            ElMessage.success('首稿已生成，可以继续优化或导入编辑器');
        } catch (error) {
            console.error('Failed to generate agent draft:', error);
            ElMessage.error('生成草稿失败，请稍后重试');
        } finally {
            generatingDraft.value = false;
        }
    };

    const optimizeCurrentDraft = async () => {
        if (!draft.value?.draftId) {
            ElMessage.warning('请先生成首稿');
            return;
        }

        const instruction = optimizeInstruction.value.trim();
        if (!instruction) {
            ElMessage.warning('请输入优化指令');
            return;
        }

        optimizingDraft.value = true;
        try {
            const response = await optimizeAgentDraft({
                draftId: draft.value.draftId,
                instruction
            });
            candidateDraft.value = normalizeAgentDraft({
                draftId: response.draftId,
                versionNo: response.candidateVersionNo,
                title: response.title,
                summary: response.summary,
                content: response.content
            });
            ElMessage.success('候选版本已生成，请先对比再决定是否采用');
        } catch (error) {
            console.error('Failed to optimize agent draft:', error);
            ElMessage.error('生成候选版本失败，请稍后重试');
        } finally {
            optimizingDraft.value = false;
        }
    };

    const adoptCandidateVersion = async () => {
        if (!draft.value?.draftId || !candidateDraft.value?.versionNo) {
            ElMessage.warning('当前没有可采用的候选版本');
            return;
        }

        adoptingCandidate.value = true;
        try {
            await adoptAgentDraftVersion({
                draftId: draft.value.draftId,
                versionNo: candidateDraft.value.versionNo
            });
            draft.value = {
                ...candidateDraft.value
            };
            candidateDraft.value = null;
            ElMessage.success('候选版本已采用');
        } catch (error) {
            console.error('Failed to adopt candidate version:', error);
            ElMessage.error('采用候选版本失败，请稍后重试');
        } finally {
            adoptingCandidate.value = false;
        }
    };

    const importDraftToEditor = async () => {
        if (!draft.value?.content) {
            ElMessage.warning('请先生成或采用一个草稿版本');
            return;
        }

        saveAgentDraftImport(draft.value);
        await router.push('/text');
    };

    return {
        loadingSession,
        creatingSession,
        chatting,
        generatingDraft,
        optimizingDraft,
        adoptingCandidate,
        session,
        sessionId,
        sessionTitle,
        chatInput,
        optimizeInstruction,
        messages,
        draft,
        candidateDraft,
        hasMessages,
        hasDraft,
        hasCandidateDraft,
        activeDraftVersion,
        pendingDraftVersion,
        draftCompareChips,
        sessionStatusLabel,
        openFreshSession,
        sendMessage,
        createDraftFromSession,
        optimizeCurrentDraft,
        adoptCandidateVersion,
        importDraftToEditor
    };
}
