import { computed, onBeforeUnmount, onMounted, ref, unref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useEditor } from '@tiptap/vue-3';

import { publishArticle, saveDraftArticle } from '@/views/article/_domain/article.js';
import { getHotAuthorTags, searchAuthorTags } from '@/views/article/_domain/tag.js';
import { ARTICLE_TYPE_ENUM } from '@/constants';
import SocketService, { formatWsUrl } from '@/utils/websocket.js';
import { API_CONFIG } from '@/config/index.js';
import { clearDraft, isDraftNewer, loadDraft, saveDraft } from '@/utils/draftStorage.js';
import {
    buildStreamingDraftPreview,
    clearAgentDraftImport,
    clearAgentGenerationIntent,
    extractArticleSummary,
    loadAgentDraftImport,
    loadAgentGenerationIntent,
    normalizeAgentDraft
} from '@/views/creator/_domain/agent.js';
import {
    AGENT_RUNTIME_COMMAND
} from '@/views/creator/_domain/stream-constants.js';
import {
    AGENT_RUN_KIND,
    AGENT_RUN_STATUS,
    AGENT_RUNTIME_EVENT,
    appendArtifactDelta,
    completeArtifactRun,
    createRunRuntime,
    failArtifactRun,
    resetRunRuntime,
    startArtifactRun,
    stopArtifactRun,
    updateArtifactStatus
} from '@/views/creator/_domain/run-runtime.js';
import {
    applyRichTextEditorAttributes,
    createRichTextEditorAttributes,
    createRichTextExtensions
} from '@/components/common/rich-text/editor-config.js';
import { buildRichTextEditorHtml } from '@/components/common/rich-text/content-pipeline.js';

import { EDITOR_CONFIG, SAVE_STATE_ENUM, SAVE_STATE_TEXT_MAP } from '../_utils/constants.js';
import { buildArticlePayload, extractTextSummarySource, hasMeaningfulContent } from '../_domain/editor.js';

import { useEditorLayout } from './useEditorLayout.js';
import { useEditorForm } from './useEditorForm.js';

function findStableMarkdownBoundary(markdown = '') {
    if (!markdown) {
        return 0;
    }

    let boundary = 0;
    let inFence = false;
    for (let index = 0; index < markdown.length; index += 1) {
        if (markdown.startsWith('```', index)) {
            inFence = !inFence;
            index += 2;
            if (!inFence) {
                boundary = index + 1;
            }
            continue;
        }

        if (!inFence && markdown.startsWith('\n\n', index)) {
            boundary = index + 2;
            index += 1;
        }
    }

    return boundary;
}

export function useEditorLogic() {
    const SPELLCHECK_STORAGE_KEY = 'chat-book-editor-spellcheck';
    const route = useRoute();
    const router = useRouter();

    const {
        layoutState,
        dragging,
        contentWidth,
        toggleLeft,
        toggleRight,
        startDrag,
        onMouseMove,
        onMouseUp
    } = useEditorLayout();

    const {
        publishDialogVisible,
        publishForm,
        authorTagOptions,
        setAuthorTags,
        setAuthorTagOptions,
        mergeAuthorTagOptions,
        handleCoverUpload,
        beforeCoverUpload
    } = useEditorForm();

    const title = ref('');
    const html = ref('');
    const wordCount = ref(0);

    const articleId = ref(route.params.id ? Number(route.params.id) : null);
    const lastSavedAt = ref(null);
    const isSaving = ref(false);
    const summaryGenerating = ref(false);
    const saveState = ref(SAVE_STATE_ENUM.SAVED);
    const hasUnsavedChanges = ref(false);
    const isHydrating = ref(false);
    const spellcheckEnabled = ref(
        typeof window !== 'undefined'
            ? window.localStorage.getItem(SPELLCHECK_STORAGE_KEY) === 'true'
            : false
    );

    const agentDraftRun = ref(createRunRuntime({ runKind: AGENT_RUN_KIND.DRAFT }));
    const aiRenderTick = ref(0);
    const aiCommittedMarkdown = ref('');
    const userEditedTitle = ref(false);
    const userEditedSummary = ref(false);

    const cacheTimer = ref(null);
    const autosaveTimer = ref(null);
    let articleSocketService = null;
    let agentSocketService = null;
    let agentSocketReadyPromise = null;
    let agentStopFallbackTimer = null;
    let ignoreAgentStream = false;

    const userId = computed(() => {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        return userInfo.id || null;
    });

    const aiGenerating = computed(() => agentDraftRun.value.status === AGENT_RUN_STATUS.RUNNING);
    const aiGenerationStopped = computed(() => agentDraftRun.value.status === AGENT_RUN_STATUS.STOPPED);
    const aiGenerationStatusText = computed(() => agentDraftRun.value.statusText || '');

    const statusText = computed(() => {
        if (aiGenerating.value) {
            return aiGenerationStatusText.value || '正在根据讨论生成初稿';
        }
        if (aiGenerationStopped.value) {
            return '已停止生成，等待你继续编辑';
        }
        return SAVE_STATE_TEXT_MAP[saveState.value] || '未保存';
    });

    const editorContentEditable = computed(() => !aiGenerating.value);

    const editor = useEditor({
        content: '',
        extensions: createRichTextExtensions({ placeholder: '请输入内容...' }),
        onUpdate: ({ editor: editorInstance }) => {
            html.value = editorInstance.getHTML();
            wordCount.value = editorInstance.storage.characterCount.characters();
        },
        editorProps: {
            attributes: createRichTextEditorAttributes({ spellcheck: spellcheckEnabled.value })
        }
    });
    const resolvedEditor = computed(() => unref(editor));

    const setSpellcheckEnabled = (enabled) => {
        spellcheckEnabled.value = enabled;
    };

    const toggleSpellcheck = () => {
        setSpellcheckEnabled(!spellcheckEnabled.value);
    };

    watch(
        [resolvedEditor, spellcheckEnabled],
        ([editorInstance, enabled]) => {
            applyRichTextEditorAttributes(editorInstance, { spellcheck: enabled });

            if (typeof window !== 'undefined') {
                window.localStorage.setItem(SPELLCHECK_STORAGE_KEY, String(enabled));
            }
        },
        { immediate: true }
    );

    const buildCurrentPayload = () => buildArticlePayload({
        articleId: articleId.value,
        title: title.value,
        html: html.value,
        publishForm: publishForm.value,
        lastSavedAt: lastSavedAt.value
    });

    const markContentDirty = ({ cacheLocal = true } = {}) => {
        hasUnsavedChanges.value = true;
        saveState.value = SAVE_STATE_ENUM.LOCAL_CACHED;

        if (!cacheLocal || !userId.value || !hasMeaningfulContent(articleId.value, title.value, html.value)) {
            return;
        }

        saveDraft(userId.value, articleId.value, buildCurrentPayload());
    };

    const normalizeAuthorTagOptions = (options = []) => (
        (options || [])
            .filter((item) => item && item.name)
            .map((item) => ({
                id: item.id ?? item.name,
                name: item.name
            }))
    );

    const loadTags = async () => {
        try {
            const hotTags = await getHotAuthorTags(20);
            setAuthorTagOptions(normalizeAuthorTagOptions(hotTags));
            mergeAuthorTagOptions(publishForm.value.authorTags || []);
        } catch (error) {
            console.error('加载作者标签失败:', error);
        }
    };

    const handleAuthorTagSearch = async (keyword) => {
        try {
            if (!keyword || !keyword.trim()) {
                await loadTags();
                return;
            }
            const matchedTags = await searchAuthorTags(keyword.trim(), 20);
            setAuthorTagOptions(normalizeAuthorTagOptions(matchedTags));
            mergeAuthorTagOptions(publishForm.value.authorTags || []);
        } catch (error) {
            console.error('搜索作者标签失败:', error);
        }
    };

    const applyPublishFormState = (sourceData = {}) => {
        publishForm.value.category = sourceData.category ?? null;
        publishForm.value.contentType = sourceData.contentType ?? 0;
        publishForm.value.abstractText = sourceData.abstractText || '';
        publishForm.value.articleType = sourceData.articleType || ARTICLE_TYPE_ENUM.ORIGINAL;
        publishForm.value.creationStatements = Array.isArray(sourceData.creationStatements)
            ? sourceData.creationStatements
            : [];
        publishForm.value.cover = sourceData.cover || '';
        publishForm.value.authorTags = Array.isArray(sourceData.authorTags) ? sourceData.authorTags : [];
        mergeAuthorTagOptions(publishForm.value.authorTags);
    };

    const setHydratingWindow = (active) => {
        isHydrating.value = active;
        if (active) {
            setTimeout(() => {
                isHydrating.value = false;
            }, 0);
        }
    };

    const applyEditorHtml = (nextHtml = '') => {
        setHydratingWindow(true);
        html.value = nextHtml;
        if (editor.value) {
            editor.value.commands.setContent(nextHtml, false);
            wordCount.value = editor.value.storage.characterCount.characters();
        }
    };

    const resetForAgentGeneration = () => {
        articleId.value = null;
        lastSavedAt.value = null;
        title.value = '';
        applyPublishFormState({});
        applyEditorHtml('');
        userEditedTitle.value = false;
        userEditedSummary.value = false;
        agentDraftRun.value = resetRunRuntime(agentDraftRun.value);
        aiCommittedMarkdown.value = '';
        aiRenderTick.value = 0;
        hasUnsavedChanges.value = false;
        saveState.value = SAVE_STATE_ENUM.SAVED;
    };

    const applyMarkdownToEditor = (markdown = '', { force = false } = {}) => {
        const nextMarkdown = force
            ? markdown
            : markdown.slice(0, findStableMarkdownBoundary(markdown));
        if (!nextMarkdown || nextMarkdown.length <= aiCommittedMarkdown.value.length) {
            return;
        }

        aiCommittedMarkdown.value = nextMarkdown;
        applyEditorHtml(buildRichTextEditorHtml(nextMarkdown, 'markdown'));
        aiRenderTick.value += 1;
        markContentDirty();
    };

    const flushRemainingMarkdown = ({ force = false } = {}) => {
        const markdown = agentDraftRun.value.finalArtifact?.content
            || agentDraftRun.value.artifactPreview?.content
            || '';
        if (!markdown) {
            return;
        }
        applyMarkdownToEditor(markdown, { force });
    };

    const clearAgentStopFallbackTimer = () => {
        if (!agentStopFallbackTimer) {
            return;
        }
        clearTimeout(agentStopFallbackTimer);
        agentStopFallbackTimer = null;
    };

    const disconnectAgentWebSocket = () => {
        clearAgentStopFallbackTimer();
        agentSocketReadyPromise = null;
        if (agentSocketService) {
            agentSocketService.close();
            agentSocketService = null;
        }
    };

    const finalizeAgentGeneration = (message) => {
        agentDraftRun.value = completeArtifactRun(agentDraftRun.value, {
            sessionId: agentDraftRun.value.sessionId,
            artifactPreview: agentDraftRun.value.artifactPreview,
            finalArtifact: agentDraftRun.value.finalArtifact ?? agentDraftRun.value.artifactPreview,
            statusText: message
        });
        disconnectAgentWebSocket();
        clearAgentGenerationIntent();
    };

    const applyCommandResult = (result, message) => {
        if (!result) return;
        articleId.value = result.articleId;
        lastSavedAt.value = result.updatedAt || null;
        isSaving.value = false;
        hasUnsavedChanges.value = false;
        saveState.value = result.status === 0 ? SAVE_STATE_ENUM.DRAFT : SAVE_STATE_ENUM.PUBLISHED;

        if (articleId.value && route.params.id !== String(articleId.value)) {
            router.replace(`/text/${articleId.value}`);
        }
        if (message) {
            ElMessage.success(message);
        }
    };

    const sendWsMessage = (type, data) => {
        const payload = data || buildCurrentPayload();
        if (articleSocketService && articleSocketService.isConnected()) {
            articleSocketService.send(type, payload);
            return true;
        }
        return false;
    };

    const submitSaveDraft = async (showMessage) => {
        if (!hasMeaningfulContent(articleId.value, title.value, html.value)) {
            isSaving.value = false;
            saveState.value = SAVE_STATE_ENUM.SAVED;
            return;
        }
        isSaving.value = true;
        saveState.value = SAVE_STATE_ENUM.SAVING;

        const payload = buildCurrentPayload();

        if (articleSocketService && articleSocketService.isConnected()) {
            try {
                const response = await articleSocketService.sendWithAck('SAVE_DRAFT', payload, {
                    timeoutMs: EDITOR_CONFIG.ACK_TIMEOUT_MS
                });
                if (userId.value) clearDraft(userId.value, articleId.value);
                applyCommandResult(response, showMessage ? '草稿已保存' : '');
                saveState.value = SAVE_STATE_ENUM.SAVED;
                if (showMessage) router.push('/');
                return;
            } catch (error) {
                console.warn('SAVE_DRAFT ACK 超时或失败，尝试 HTTP 降级:', error.message);
            }
        }

        try {
            const result = await saveDraftArticle(payload);
            if (userId.value) clearDraft(userId.value, articleId.value);
            applyCommandResult(result, showMessage ? '草稿已保存' : '');
            saveState.value = SAVE_STATE_ENUM.SAVED;
            if (showMessage) router.push('/');
        } catch (error) {
            console.error('HTTP 降级保存失败:', error);
            saveState.value = SAVE_STATE_ENUM.ERROR;
            ElMessage.error('保存失败，请重试');
        } finally {
            isSaving.value = false;
        }
    };

    const submitPublish = async () => {
        if (!hasMeaningfulContent(articleId.value, title.value, html.value)) {
            ElMessage.warning('文章内容不能为空');
            return;
        }
        isSaving.value = true;
        saveState.value = SAVE_STATE_ENUM.SAVING;

        const payload = buildCurrentPayload();

        if (articleSocketService && articleSocketService.isConnected()) {
            try {
                const response = await articleSocketService.sendWithAck('PUBLISH', payload, {
                    timeoutMs: EDITOR_CONFIG.ACK_TIMEOUT_MS
                });
                if (userId.value) clearDraft(userId.value, articleId.value);
                applyCommandResult(response, '发布成功');
                saveState.value = SAVE_STATE_ENUM.PUBLISHED;
                router.push('/');
                return;
            } catch (error) {
                console.warn('PUBLISH ACK 超时或失败，尝试 HTTP 降级:', error.message);
            }
        }

        try {
            const result = await publishArticle(payload);
            if (userId.value) clearDraft(userId.value, articleId.value);
            applyCommandResult(result, '发布成功');
            saveState.value = SAVE_STATE_ENUM.PUBLISHED;
            router.push('/');
        } catch (error) {
            console.error('HTTP 发布失败:', error);
            saveState.value = SAVE_STATE_ENUM.ERROR;
            ElMessage.error('发布失败，请重试');
        } finally {
            isSaving.value = false;
        }
    };

    const queueSaveFlow = () => {
        clearTimeout(cacheTimer.value);
        clearTimeout(autosaveTimer.value);
        hasUnsavedChanges.value = true;

        const payload = buildCurrentPayload();
        if (userId.value && hasMeaningfulContent(articleId.value, title.value, html.value)) {
            saveDraft(userId.value, articleId.value, payload);
        }

        saveState.value = SAVE_STATE_ENUM.LOCAL_CACHED;
        cacheTimer.value = setTimeout(() => {
            sendWsMessage('CACHE', payload);
        }, EDITOR_CONFIG.CACHE_DELAY_MS);

        autosaveTimer.value = setTimeout(() => {
            submitSaveDraft(false);
        }, EDITOR_CONFIG.AUTOSAVE_DELAY_MS);
    };

    const confirmPublish = () => {
        if (!title.value) return ElMessage.warning('请输入标题');
        if (!publishForm.value.authorTags || publishForm.value.authorTags.length === 0) {
            return ElMessage.warning('请至少填写一个作者标签');
        }
        if (!publishForm.value.articleType) {
            return ElMessage.warning('请选择文章类型');
        }
        publishDialogVisible.value = false;
        submitPublish();
    };

    const handleExtractSummary = async () => {
        const contentSource = extractTextSummarySource(html.value);
        if (!contentSource) {
            ElMessage.warning('请先输入正文内容');
            return;
        }

        summaryGenerating.value = true;
        try {
            const result = await extractArticleSummary({
                title: title.value,
                content: contentSource
            });
            publishForm.value.abstractText = result?.summary || '';
            if (publishForm.value.abstractText) {
                ElMessage.success('AI 摘要已提取');
            } else {
                ElMessage.warning('未提取到可用摘要，请手动补充');
            }
        } catch (error) {
            console.error('提取摘要失败:', error);
            ElMessage.error('AI 摘要提取失败，请稍后重试');
        } finally {
            summaryGenerating.value = false;
        }
    };

    const applyImportedDraft = (payload) => {
        if (!payload) {
            return;
        }

        articleId.value = null;
        lastSavedAt.value = null;
        title.value = payload.title || '';
        applyPublishFormState(payload);
        applyEditorHtml(payload.content || '');
        hasUnsavedChanges.value = true;
        saveState.value = SAVE_STATE_ENUM.LOCAL_CACHED;
    };

    const connectArticleWebSocket = () => {
        const token = localStorage.getItem('token');
        const wsUrl = formatWsUrl(API_CONFIG.baseURL);
        articleSocketService = new SocketService(`${wsUrl}/api/article/ws`, token);

        articleSocketService.onOpen(() => {
            if (articleId.value) sendWsMessage('SELECT');
        });

        articleSocketService.onClose(() => console.log('已断开与文章服务器的连接'));
        articleSocketService.onError((error) => console.log('错误: ' + error.message));

        articleSocketService.on('CACHE', () => {
            isSaving.value = false;
            saveState.value = SAVE_STATE_ENUM.CACHED;
        });
        articleSocketService.on('SAVE_DRAFT', (data) => applyCommandResult(data, '草稿已保存'));
        articleSocketService.on('PUBLISH', (data) => {
            applyCommandResult(data, '发布成功');
            router.push('/');
        });
        articleSocketService.on('SELECT', async (data) => {
            if (!data) return;

            let restoreLocalDraft = false;
            if (userId.value && articleId.value) {
                const localDraft = loadDraft(userId.value, articleId.value);
                if (localDraft && isDraftNewer(localDraft, data.updatedAt)) {
                    try {
                        await ElMessageBox.confirm('检测到本地草稿比服务器更新，是否恢复本地版本？', '发现较新草稿', {
                            confirmButtonText: '恢复本地',
                            cancelButtonText: '使用服务器版本',
                            distinguishCancelAndClose: true
                        });
                        restoreLocalDraft = true;
                    } catch (error) {
                        clearDraft(userId.value, articleId.value);
                    }
                }
            }

            const sourceData = restoreLocalDraft ? loadDraft(userId.value, articleId.value) : data;
            setHydratingWindow(!restoreLocalDraft);
            articleId.value = sourceData.id || articleId.value;
            title.value = sourceData.title || '';
            applyPublishFormState(sourceData);

            if (!restoreLocalDraft) lastSavedAt.value = sourceData.updatedAt || null;
            saveState.value = restoreLocalDraft
                ? SAVE_STATE_ENUM.LOCAL_CACHED
                : (sourceData.status === 0 ? SAVE_STATE_ENUM.DRAFT : SAVE_STATE_ENUM.PUBLISHED);
            hasUnsavedChanges.value = restoreLocalDraft;

            applyEditorHtml(sourceData.content || '');
        });

        articleSocketService.connect();
    };

    const connectAgentWebSocket = () => {
        if (agentSocketService) {
            return agentSocketService;
        }

        const token = localStorage.getItem('token');
        const wsUrl = formatWsUrl(API_CONFIG.baseURL);
        agentSocketService = new SocketService(`${wsUrl}/api/agent/ws`, token, {
            maxReconnectAttempts: 0
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_STARTED, (payload = {}) => {
            if (ignoreAgentStream) {
                return;
            }
            agentDraftRun.value = startArtifactRun(agentDraftRun.value, {
                runId: payload.sessionId ?? agentDraftRun.value.runId,
                sessionId: payload.sessionId ?? agentDraftRun.value.sessionId,
                source: 'editor',
                statusText: payload.statusText || '正在整理讨论上下文...'
            });
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_STATUS, (payload = {}) => {
            if (ignoreAgentStream) {
                return;
            }
            agentDraftRun.value = updateArtifactStatus(
                aiGenerating.value
                    ? agentDraftRun.value
                    : startArtifactRun(agentDraftRun.value, {
                        runId: payload.sessionId ?? agentDraftRun.value.runId,
                        sessionId: payload.sessionId ?? agentDraftRun.value.sessionId,
                        source: 'editor'
                    }),
                payload.statusText || aiGenerationStatusText.value || '正在生成正文...'
            );
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_DELTA, (payload = {}) => {
            if (ignoreAgentStream || typeof payload.chunk !== 'string' || !payload.chunk) {
                return;
            }

            agentDraftRun.value = appendArtifactDelta(agentDraftRun.value, {
                chunk: payload.chunk,
                statusText: aiGenerationStatusText.value || '正在生成正文...',
                buildPreview: buildStreamingDraftPreview
            });
            const preview = agentDraftRun.value.artifactPreview;
            if (!preview?.content) {
                return;
            }

            applyMarkdownToEditor(preview.content);
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_COMPLETED, (payload = {}) => {
            if (ignoreAgentStream) {
                return;
            }

            const finalArtifact = normalizeAgentDraft(payload.finalArtifact || {});
            agentDraftRun.value = completeArtifactRun(agentDraftRun.value, {
                sessionId: payload.sessionId ?? agentDraftRun.value.sessionId,
                artifactPreview: normalizeAgentDraft({
                    ...finalArtifact,
                    content: finalArtifact.content || agentDraftRun.value.artifactPreview?.content || ''
                }),
                finalArtifact: normalizeAgentDraft({
                    ...finalArtifact,
                    content: finalArtifact.content || agentDraftRun.value.artifactPreview?.content || ''
                }),
                statusText: '初稿已生成，可继续编辑'
            });
            flushRemainingMarkdown({ force: true });

            if (!userEditedTitle.value && finalArtifact.title) {
                title.value = finalArtifact.title;
            }
            if (!userEditedSummary.value && finalArtifact.summary) {
                publishForm.value.abstractText = finalArtifact.summary;
            }

            markContentDirty();
            finalizeAgentGeneration('初稿已生成，可继续编辑');
            ElMessage.success('初稿已生成，可继续编辑');
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_FAILED, (payload = {}) => {
            if (ignoreAgentStream) {
                return;
            }
            flushRemainingMarkdown({ force: true });
            agentDraftRun.value = failArtifactRun(agentDraftRun.value, payload.message || '初稿生成失败，请稍后重试');
            disconnectAgentWebSocket();
            clearAgentGenerationIntent();
            ElMessage.error(payload.message || '初稿生成失败，请稍后重试');
        });

        agentSocketService.on(AGENT_RUNTIME_EVENT.ARTIFACT_STOPPED, (payload = {}) => {
            agentDraftRun.value = stopArtifactRun(agentDraftRun.value, payload.statusText || '已停止生成，你可以直接接管正文');
            disconnectAgentWebSocket();
            clearAgentGenerationIntent();
        });

        agentSocketService.onClose(() => {
            if (!aiGenerating.value || aiGenerationStopped.value || ignoreAgentStream) {
                return;
            }
            flushRemainingMarkdown({ force: true });
            agentDraftRun.value = failArtifactRun(agentDraftRun.value, '生成连接已断开，已保留当前内容');
            clearAgentGenerationIntent();
            ElMessage.error('生成连接已断开，已保留当前内容');
        });

        agentSocketService.onError((error) => {
            console.error('Agent WebSocket error:', error);
        });

        agentSocketService.connect();
        return agentSocketService;
    };

    const ensureAgentSocketReady = async (timeoutMs = 5000) => {
        const service = connectAgentWebSocket();
        if (service.isConnected()) {
            return service;
        }
        if (agentSocketReadyPromise) {
            return agentSocketReadyPromise;
        }

        agentSocketReadyPromise = new Promise((resolve, reject) => {
            const startedAt = Date.now();
            const timer = setInterval(() => {
                if (service.isConnected()) {
                    clearInterval(timer);
                    agentSocketReadyPromise = null;
                    resolve(service);
                    return;
                }

                const readyState = service?.socket?.readyState;
                if (readyState === WebSocket.CLOSING || readyState === WebSocket.CLOSED) {
                    clearInterval(timer);
                    agentSocketReadyPromise = null;
                    reject(new Error('Agent WebSocket handshake failed'));
                    return;
                }

                if (Date.now() - startedAt >= timeoutMs) {
                    clearInterval(timer);
                    agentSocketReadyPromise = null;
                    reject(new Error('Agent WebSocket connect timeout'));
                }
            }, 100);
        });

        return agentSocketReadyPromise;
    };

    const startAgentDraftGeneration = async (sessionId) => {
        if (!sessionId || aiGenerating.value) {
            return;
        }

        ignoreAgentStream = false;
        agentDraftRun.value = startArtifactRun(agentDraftRun.value, {
            runId: Number(sessionId),
            sessionId: Number(sessionId),
            source: 'editor',
            statusText: '正在连接生成通道...'
        });
        aiCommittedMarkdown.value = '';
        aiRenderTick.value = 0;

        try {
            const service = await ensureAgentSocketReady();
            const sent = service.send(AGENT_RUNTIME_COMMAND.ARTIFACT_GENERATE, { sessionId });
            if (!sent) {
                throw new Error('Agent WebSocket 未连接');
            }
        } catch (error) {
            console.error('Failed to start agent draft generation:', error);
            agentDraftRun.value = failArtifactRun(agentDraftRun.value, '初稿生成启动失败，请稍后重试');
            disconnectAgentWebSocket();
            clearAgentGenerationIntent();
            ElMessage.error('初稿生成启动失败，请稍后重试');
        }
    };

    const stopAgentDraftGeneration = () => {
        if (!aiGenerating.value) {
            return;
        }

        ignoreAgentStream = true;
        agentDraftRun.value = stopArtifactRun(agentDraftRun.value, '已停止生成，你可以直接接管正文');
        flushRemainingMarkdown({ force: true });
        clearAgentGenerationIntent();
        markContentDirty();

        const sessionId = agentDraftRun.value.sessionId;
        const sent = sessionId && agentSocketService
            ? agentSocketService.send(AGENT_RUNTIME_COMMAND.ARTIFACT_STOP, { sessionId })
            : false;

        if (!sent) {
            disconnectAgentWebSocket();
            return;
        }

        clearAgentStopFallbackTimer();
        agentStopFallbackTimer = setTimeout(() => {
            disconnectAgentWebSocket();
        }, 1500);
    };

    const handleUserTitleInput = () => {
        userEditedTitle.value = true;
        queueSaveFlow();
    };

    const handleUserSummaryInput = () => {
        userEditedSummary.value = true;
        queueSaveFlow();
    };

    const handleBeforeUnload = (event) => {
        if (hasUnsavedChanges.value) {
            if (userId.value) {
                saveDraft(userId.value, articleId.value, buildCurrentPayload());
            }
            event.preventDefault();
            event.returnValue = '';
        }
    };

    const confirmNavigation = async () => {
        if (!hasUnsavedChanges.value) return true;
        const action = await ElMessageBox.confirm('您有未保存的修改，是否保存草稿？', '提示', {
            confirmButtonText: '保存并离开',
            cancelButtonText: '直接离开',
            type: 'warning',
            distinguishCancelAndClose: true
        }).catch(action => action);

        if (action === 'confirm') {
            await submitSaveDraft(false);
            return true;
        }
        if (action === 'cancel') {
            return true;
        }
        return false;
    };

    watch(html, (newVal, oldVal) => {
        if (!isHydrating.value && newVal !== oldVal) {
            queueSaveFlow();
        }
    });

    onMounted(async () => {
        window.addEventListener('beforeunload', handleBeforeUnload);
        await loadTags();
        connectArticleWebSocket();

        const generationIntent = !articleId.value ? loadAgentGenerationIntent() : null;
        if (generationIntent?.sessionId) {
            clearAgentGenerationIntent();
            resetForAgentGeneration();
            startAgentDraftGeneration(generationIntent.sessionId);
            return;
        }

        if (!articleId.value) {
            const importedDraft = loadAgentDraftImport();
            if (importedDraft && hasMeaningfulContent(articleId.value, importedDraft.title, importedDraft.content)) {
                applyImportedDraft(importedDraft);
                clearAgentDraftImport();
                ElMessage.success('已导入 Agent 草稿，请继续润色并保存');
                return;
            }
        }

        if (!articleId.value && userId.value) {
            const draft = loadDraft(userId.value, 'new');
            if (draft && hasMeaningfulContent(articleId.value, draft.title, draft.content)) {
                try {
                    await ElMessageBox.confirm('检测到未同步的本地草稿，是否恢复？', '恢复草稿', {
                        confirmButtonText: '恢复草稿',
                        cancelButtonText: '放弃',
                        distinguishCancelAndClose: true
                    });
                    title.value = draft.title || '';
                    applyPublishFormState(draft);
                    applyEditorHtml(draft.content || '');
                    hasUnsavedChanges.value = true;
                    saveState.value = SAVE_STATE_ENUM.LOCAL_CACHED;
                } catch (error) {
                    clearDraft(userId.value, 'new');
                }
            }
        }
    });

    onBeforeUnmount(() => {
        window.removeEventListener('beforeunload', handleBeforeUnload);
        clearTimeout(cacheTimer.value);
        clearTimeout(autosaveTimer.value);
        if (editor.value) {
            editor.value.destroy();
        }
        if (articleSocketService) {
            articleSocketService.close();
            articleSocketService = null;
        }
        disconnectAgentWebSocket();
    });

    return {
        title,
        html,
        wordCount,
        publishDialogVisible,
        publishForm,
        authorTagOptions,
        layoutState,
        dragging,
        isSaving,
        summaryGenerating,
        saveState,
        statusText,
        contentWidth,
        editor,
        spellcheckEnabled,
        hasUnsavedChanges,
        userId,
        articleId,
        aiGenerating,
        aiGenerationStopped,
        aiGenerationStatusText,
        aiRenderTick,
        editorContentEditable,

        setAuthorTags,
        handleAuthorTagSearch,
        handleCoverUpload,
        beforeCoverUpload,
        handleExtractSummary,
        toggleLeft,
        toggleRight,
        startDrag,
        onMouseMove,
        onMouseUp,
        toggleSpellcheck,
        queueSaveFlow,
        submitSaveDraft,
        confirmPublish,
        confirmNavigation,
        clearDraft,
        stopAgentDraftGeneration,
        handleUserTitleInput,
        handleUserSummaryInput
    };
}
