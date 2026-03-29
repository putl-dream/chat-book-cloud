import {computed, onBeforeUnmount, onMounted, ref, unref, watch} from 'vue';
import {useRoute, useRouter} from 'vue-router';
import {ElMessage, ElMessageBox} from 'element-plus';
import {useEditor} from '@tiptap/vue-3';

import {publishArticle, saveDraftArticle} from '@/views/article/_domain/article.js';
import {getTagsByType} from '@/views/article/_domain/tag.js';
import {TAG_TYPE_ENUM} from '@/constants';
import SocketService, {formatWsUrl} from '@/utils/websocket.js';
import {API_CONFIG} from '@/config/index.js';
import {clearDraft, isDraftNewer, loadDraft, saveDraft} from '@/utils/draftStorage.js';
import {applyRichTextEditorAttributes, createRichTextEditorAttributes, createRichTextExtensions} from '@/components/common/rich-text/editor-config.js';

import {EDITOR_CONFIG, SAVE_STATE_ENUM, SAVE_STATE_TEXT_MAP} from '../_utils/constants.js';
import {buildArticlePayload, hasMeaningfulContent} from '../_domain/editor.js';

import {useEditorLayout} from './useEditorLayout.js';
import {useEditorForm} from './useEditorForm.js';

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
        techTags,
        pathTags,
        selectedTechTags,
        selectedPathTag,
        updateTagIds,
        handleCoverUpload,
        beforeCoverUpload
    } = useEditorForm();

    // =============== View State ===============
    const title = ref('');
    const html = ref('');
    const wordCount = ref(0);

    // =============== Hook State ===============
    const articleId = ref(route.params.id ? Number(route.params.id) : null);
    const lastSavedAt = ref(null);
    const isSaving = ref(false);
    const saveState = ref(SAVE_STATE_ENUM.SAVED);
    const hasUnsavedChanges = ref(false);
    const isHydrating = ref(false);
    const spellcheckEnabled = ref(
        typeof window !== 'undefined'
            ? window.localStorage.getItem(SPELLCHECK_STORAGE_KEY) === 'true'
            : false
    );

    const cacheTimer = ref(null);
    const autosaveTimer = ref(null);
    let socketService = null;

    const userId = computed(() => {
        const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        return userInfo.id || null;
    });

    const statusText = computed(() => SAVE_STATE_TEXT_MAP[saveState.value] || '未保存');

    // =============== Editor Setup ===============
    const editor = useEditor({
        content: '',
        extensions: createRichTextExtensions({ placeholder: '请输入内容...' }),
        onUpdate: ({ editor }) => {
            html.value = editor.getHTML();
            wordCount.value = editor.storage.characterCount.characters();
        },
        editorProps: {
            attributes: createRichTextEditorAttributes({ spellcheck: spellcheckEnabled.value }),
        },
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

    // =============== Actions / Domain Integration ===============
    const loadTags = async () => {
        try {
            const [techRes, pathRes] = await Promise.all([
                getTagsByType(TAG_TYPE_ENUM.TECH),
                getTagsByType(TAG_TYPE_ENUM.PATH)
            ]);
            techTags.value = techRes || [];
            pathTags.value = pathRes || [];
        } catch (error) {
            console.error('加载标签失败:', error);
        }
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
        if (!data) {
            data = buildArticlePayload({ articleId: articleId.value, title: title.value, html: html.value, publishForm: publishForm.value, lastSavedAt: lastSavedAt.value });
        }
        if (socketService && socketService.isConnected()) {
            console.log("发送消息->>", type, data);
            socketService.send(type, data);
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

        const payload = buildArticlePayload({ articleId: articleId.value, title: title.value, html: html.value, publishForm: publishForm.value, lastSavedAt: lastSavedAt.value });

        if (socketService && socketService.isConnected()) {
            try {
                const response = await socketService.sendWithAck('SAVE_DRAFT', payload, { timeoutMs: EDITOR_CONFIG.ACK_TIMEOUT_MS });
                if (userId.value) clearDraft(userId.value, articleId.value);
                applyCommandResult(response, showMessage ? '草稿已保存' : '');
                saveState.value = SAVE_STATE_ENUM.SAVED;
                if (showMessage) router.push('/');
                return;
            } catch (e) {
                console.warn('SAVE_DRAFT ACK 超时或失败，尝试 HTTP 降级:', e.message);
            }
        }

        try {
            const result = await saveDraftArticle(payload);
            if (userId.value) clearDraft(userId.value, articleId.value);
            applyCommandResult(result, showMessage ? '草稿已保存' : '');
            saveState.value = SAVE_STATE_ENUM.SAVED;
            if (showMessage) router.push('/');
        } catch (e) {
            console.error('HTTP 降级保存失败:', e);
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

        const payload = buildArticlePayload({ articleId: articleId.value, title: title.value, html: html.value, publishForm: publishForm.value, lastSavedAt: lastSavedAt.value });

        if (socketService && socketService.isConnected()) {
            try {
                const response = await socketService.sendWithAck('PUBLISH', payload, { timeoutMs: EDITOR_CONFIG.ACK_TIMEOUT_MS });
                if (userId.value) clearDraft(userId.value, articleId.value);
                applyCommandResult(response, '发布成功');
                saveState.value = SAVE_STATE_ENUM.PUBLISHED;
                router.push('/');
                return;
            } catch (e) {
                console.warn('PUBLISH ACK 超时或失败，尝试 HTTP 降级:', e.message);
            }
        }

        try {
            const result = await publishArticle(payload);
            if (userId.value) clearDraft(userId.value, articleId.value);
            applyCommandResult(result, '发布成功');
            saveState.value = SAVE_STATE_ENUM.PUBLISHED;
            router.push('/');
        } catch (e) {
            console.error('HTTP 发布失败:', e);
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

        const payload = buildArticlePayload({ articleId: articleId.value, title: title.value, html: html.value, publishForm: publishForm.value, lastSavedAt: lastSavedAt.value });
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
        if (!publishForm.value.category) return ElMessage.warning('请选择分类');
        publishDialogVisible.value = false;
        submitPublish();
    };

    // =============== WebSocket Setup ===============
    const connectWebSocket = () => {
        const token = localStorage.getItem('token');
        const wsUrl = formatWsUrl(API_CONFIG.baseURL);
        socketService = new SocketService(`${wsUrl}/api/article/ws`, token);

        socketService.onOpen(() => {
            console.log('已连接到服务器');
            if (articleId.value) sendWsMessage('SELECT');
        });

        socketService.onClose(() => console.log('已断开与服务器的连接'));
        socketService.onError((error) => console.log('错误: ' + error.message));

        socketService.on('CACHE', () => {
            isSaving.value = false;
            saveState.value = SAVE_STATE_ENUM.CACHED;
        });
        socketService.on('SAVE_DRAFT', (data) => applyCommandResult(data, '草稿已保存'));
        socketService.on('PUBLISH', (data) => {
            applyCommandResult(data, '发布成功');
            router.push('/');
        });
        socketService.on('SELECT', async (data) => {
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
                    } catch (e) {
                        clearDraft(userId.value, articleId.value);
                    }
                }
            }

            const sourceData = restoreLocalDraft ? loadDraft(userId.value, articleId.value) : data;

            isHydrating.value = !restoreLocalDraft;
            articleId.value = sourceData.id || articleId.value;
            title.value = sourceData.title || '';
            html.value = sourceData.content || '';
            publishForm.value.category = sourceData.category ?? null;
            publishForm.value.contentType = sourceData.contentType ?? 0;
            publishForm.value.abstractText = sourceData.abstractText || '';
            publishForm.value.cover = sourceData.cover || '';
            publishForm.value.tagIds = sourceData.tagIds || [];

            if (!restoreLocalDraft) lastSavedAt.value = sourceData.updatedAt || null;
            saveState.value = restoreLocalDraft ? SAVE_STATE_ENUM.LOCAL_CACHED : (sourceData.status === 0 ? SAVE_STATE_ENUM.DRAFT : SAVE_STATE_ENUM.PUBLISHED);
            hasUnsavedChanges.value = restoreLocalDraft;

            if (sourceData.tagIds && sourceData.tagIds.length > 0) {
                const techIds = techTags.value.filter(t => t.type === TAG_TYPE_ENUM.TECH).map(t => t.id);
                const pathIds = pathTags.value.filter(t => t.type === TAG_TYPE_ENUM.PATH).map(t => t.id);
                selectedTechTags.value = sourceData.tagIds.filter(id => techIds.includes(id));
                selectedPathTag.value = sourceData.tagIds.find(id => pathIds.includes(id)) || null;
            }

            if (editor.value) {
                editor.value.commands.setContent(sourceData.content || '', false);
                wordCount.value = editor.value.storage.characterCount.characters();
            }
            isHydrating.value = false;
        });

        socketService.connect();
    };

    const handleBeforeUnload = (e) => {
        if (hasUnsavedChanges.value) {
            if (userId.value) {
                const payload = buildArticlePayload({ articleId: articleId.value, title: title.value, html: html.value, publishForm: publishForm.value, lastSavedAt: lastSavedAt.value });
                saveDraft(userId.value, articleId.value, payload);
            }
            e.preventDefault();
            e.returnValue = '';
        }
    };

    const confirmNavigation = async (to) => {
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
        } else if (action === 'cancel') {
            return true;
        }
        return false;
    };

    // =============== Lifecycle ===============
    watch(html, (newVal, oldVal) => {
        if (!isHydrating.value && newVal !== oldVal) {
            queueSaveFlow();
        }
    });

    onMounted(async () => {
        window.addEventListener('beforeunload', handleBeforeUnload);
        connectWebSocket();
        loadTags();

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
                    html.value = draft.content || '';
                    publishForm.value.category = draft.category ?? null;
                    publishForm.value.contentType = draft.contentType ?? 0;
                    publishForm.value.abstractText = draft.abstractText || '';
                    publishForm.value.cover = draft.cover || '';
                    publishForm.value.tagIds = draft.tagIds || [];

                    if (editor.value) {
                        editor.value.commands.setContent(draft.content || '', false);
                        wordCount.value = editor.value.storage.characterCount.characters();
                    }
                    hasUnsavedChanges.value = true;
                    saveState.value = SAVE_STATE_ENUM.LOCAL_CACHED;
                } catch (e) {
                    clearDraft(userId.value, 'new');
                }
            }
        }
    });

    onBeforeUnmount(() => {
        window.removeEventListener('beforeunload', handleBeforeUnload);
        clearTimeout(cacheTimer.value);
        clearTimeout(autosaveTimer.value);
        if (editor.value) editor.value.destroy();
        if (socketService) {
            socketService.close();
            socketService = null;
        }
    });

    return {
        // State
        title,
        html,
        wordCount,
        publishDialogVisible,
        publishForm,
        layoutState,
        dragging,
        techTags,
        pathTags,
        selectedTechTags,
        selectedPathTag,
        isSaving,
        saveState,
        statusText,
        contentWidth,
        editor,
        spellcheckEnabled,
        hasUnsavedChanges,
        userId,
        articleId,

        // Actions
        updateTagIds,
        handleCoverUpload,
        beforeCoverUpload,
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
        clearDraft
    };
}
