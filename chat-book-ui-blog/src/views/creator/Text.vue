<template>
    <div class="editor-layout">
        <CreativeHeader class="site-header" />

        <div class="text-toolbar">
            <div class="toolbar-wrapper">
                <TiptapToolbar :editor="editor" class="glass-toolbar" v-if="editor" @toggle-toc="toggleToc"
                    :tocVisible="tocVisible" />
                <div class="status-bar">
                    <div class="status-indicator" :class="{ 'saving': save }"></div>
                    <el-text class="status-text">{{ statusText }}</el-text>
                </div>
            </div>
        </div>

        <div class="editor-container">
            <ArticleToc v-if="editor && tocVisible" :editor="editor" />

            <div class="scroll-area">
                <div class="main-content">
                    <!-- 标题区域 -->
                    <div class="title-area">
                        <input type="text" v-model="title" placeholder="请输入文章标题" class="title-input" @input="onInput" />
                    </div>

                    <!-- 内容区域 -->
                    <editor-content :editor="editor" class="main-content-editor" />
                </div>
            </div>
        </div>

        <div class="footer-card">
            <div class="word-count">
                <el-text>共 {{ wordCount }} 字</el-text>
            </div>
            <div class="buttons">
                <button class="action-btn save" @click="saveContent">保存草稿</button>
                <button class="action-btn publish" @click="publishContent">发布文章</button>
            </div>
        </div>
        <el-dialog v-model="publishDialogVisible" title="发布文章" width="500px">
            <el-form :model="publishForm" label-width="80px">
                <el-form-item label="文章分类">
                    <el-select v-model="publishForm.category" placeholder="请选择分类">
                        <el-option v-for="(name, key) in categoryOptions" :key="key" :label="name"
                            :value="parseInt(key)" />
                    </el-select>
                </el-form-item>
                <el-form-item label="文章摘要">
                    <el-input v-model="publishForm.abstractText" type="textarea" :rows="3" placeholder="请输入文章摘要" />
                </el-form-item>
                <el-form-item label="文章封面">
                    <el-upload class="avatar-uploader" action="" :http-request="handleUpload" :show-file-list="false"
                        :before-upload="beforeAvatarUpload">
                        <img v-if="publishForm.cover" :src="publishForm.cover" class="avatar"
                            style="width: 100px; height: 100px; object-fit: cover;" />
                        <el-icon v-else class="avatar-uploader-icon"
                            style="border: 1px dashed #d9d9d9; border-radius: 6px; cursor: pointer; position: relative; overflow: hidden; width: 100px; height: 100px; display: flex; justify-content: center; align-items: center;">
                            <Plus />
                        </el-icon>
                    </el-upload>
                </el-form-item>
            </el-form>
            <template #footer>
                <span class="dialog-footer">
                    <el-button @click="publishDialogVisible = false">取消</el-button>
                    <el-button type="primary" @click="confirmPublish">确定发布</el-button>
                </span>
            </template>
        </el-dialog>
    </div>
</template>

<script setup>
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue';
import CreativeHeader from "@/components/domain/CreativeHeader.vue";
import TiptapToolbar from "@/components/domain/TiptapToolbar.vue";
import ArticleToc from "@/components/domain/ArticleToc.vue";
import { ElMessage } from "element-plus";
import { onBeforeRouteUpdate, useRoute, useRouter } from "vue-router";
import SocketService, { formatWsUrl } from "@/utils/websocket.js";
import { API_CONFIG } from "@/config/index.js";
import { ElDialog, ElForm, ElFormItem, ElSelect, ElOption, ElInput, ElUpload, ElButton, ElIcon } from 'element-plus';
import { CATEGORY_NAMES } from '@/constants';
import { publishArticle, saveDraftArticle, uploadFile } from '@/api/article.js';
import { Plus } from '@element-plus/icons-vue';

import { EditorContent, useEditor } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Placeholder from '@tiptap/extension-placeholder';
import CharacterCount from '@tiptap/extension-character-count';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Highlight from '@tiptap/extension-highlight';
import { Color } from '@tiptap/extension-color';
import { TextStyle } from '@tiptap/extension-text-style';
import TaskList from '@tiptap/extension-task-list';
import TaskItem from '@tiptap/extension-task-item';
import SlashCommand from '@/components/domain/slash-command/index.js';
import suggestion from '@/components/domain/slash-command/suggestion.js';

const route = useRoute();
const router = useRouter();

const title = ref('');
const html = ref('');
const wordCount = ref(0);
const articleId = ref(route.params.id ? Number(route.params.id) : null);
const lastSavedAt = ref(null);
const save = ref(false);
const saveState = ref('idle');
const hasUnsavedChanges = ref(false);
const publishDialogVisible = ref(false);
const publishForm = ref({
    category: null,
    abstractText: '',
    cover: ''
});
const categoryOptions = CATEGORY_NAMES;
const cacheTimer = ref(null);
const autosaveTimer = ref(null);
const hydrating = ref(false);
const tocVisible = ref(true);

const toggleToc = () => {
    tocVisible.value = !tocVisible.value;
};

const statusText = computed(() => {
    if (save.value) {
        return '保存中...';
    }
    if (saveState.value === 'cached') {
        return '已缓存';
    }
    if (saveState.value === 'draft') {
        return '草稿已保存';
    }
    if (saveState.value === 'published') {
        return '发布成功';
    }
    return '未保存';
});

const handleUpload = async (option) => {
    try {
        const res = await uploadFile(option.file);
        if (res && res.url) {
            publishForm.value.cover = res.url;
            ElMessage.success('上传成功');
        } else {
            ElMessage.error('上传失败');
        }
    } catch (e) {
        console.error(e);
        ElMessage.error('上传出错');
    }
};

const beforeAvatarUpload = (rawFile) => {
    if (rawFile.type !== 'image/jpeg' && rawFile.type !== 'image/png') {
        ElMessage.error('Avatar picture must be JPG format!');
        return false;
    }
    if (rawFile.size / 1024 / 1024 > 2) {
        ElMessage.error('Avatar picture size can not exceed 2MB!');
        return false;
    }
    return true;
};

const editor = useEditor({
    content: '',
    extensions: [
        StarterKit,
        Image,
        Underline,
        Highlight.configure({ multicolor: true }),
        TextStyle,
        Color,
        TaskList,
        TaskItem.configure({
            nested: true,
        }),
        SlashCommand.configure({
            suggestion,
        }),
        Placeholder.configure({
            placeholder: '请输入内容...',
        }),
        CharacterCount,
        TextAlign.configure({
            types: ['heading', 'paragraph'],
        }),
    ],
    onUpdate: ({ editor }) => {
        html.value = editor.getHTML();
        wordCount.value = editor.storage.characterCount.characters();
    },
    editorProps: {
        attributes: {
            class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none',
        },
    },
});

let socketService;

function buildPayload() {
    return {
        id: articleId.value,
        title: title.value,
        content: html.value,
        category: publishForm.value.category,
        abstractText: publishForm.value.abstractText,
        cover: publishForm.value.cover,
        updatedAt: lastSavedAt.value
    };
}

function hasMeaningfulContent() {
    return Boolean(articleId.value || title.value?.trim() || html.value?.replace(/<[^>]+>/g, '').trim());
}

function applyCommandResult(result, message) {
    if (!result) {
        return;
    }
    articleId.value = result.articleId;
    lastSavedAt.value = result.updatedAt || null;
    save.value = false;
    hasUnsavedChanges.value = false;
    saveState.value = result.status === 0 ? 'draft' : 'published';
    if (articleId.value && route.params.id !== String(articleId.value)) {
        router.replace(`/text/${articleId.value}`);
    }
    if (message) {
        ElMessage.success(message);
    }
}

function sendMessage(type) {
    const data = buildPayload();
    if (socketService && socketService.isConnected()) {
        console.log("发送消息-->>", type, data);
        socketService.send(type, data);
        return true;
    }
    console.log('WebSocket 未打开');
    return false;
}

async function saveContentViaHttp() {
    const result = await saveDraftArticle(buildPayload());
    applyCommandResult(result, '');
}

async function submitSaveDraft(showMessage) {
    if (!hasMeaningfulContent()) {
        save.value = false;
        return;
    }
    save.value = true;
    if (sendMessage('SAVE_DRAFT')) {
        return;
    }
    try {
        await saveContentViaHttp();
        if (showMessage) {
            ElMessage.success('草稿已保存');
        }
    } catch (e) {
        console.error('HTTP 降级保存失败:', e);
    } finally {
        save.value = false;
    }
}

async function submitPublish() {
    if (!hasMeaningfulContent()) {
        ElMessage.warning('文章内容不能为空');
        return;
    }
    save.value = true;
    if (sendMessage('PUBLISH')) {
        return;
    }
    try {
        const result = await publishArticle(buildPayload());
        applyCommandResult(result, '发布成功');
        router.push('/');
    } catch (e) {
        console.error('HTTP 发布失败:', e);
    } finally {
        save.value = false;
    }
}

function queueSaveFlow() {
    clearTimeout(cacheTimer.value);
    clearTimeout(autosaveTimer.value);
    hasUnsavedChanges.value = true;
    save.value = true;
    cacheTimer.value = setTimeout(() => {
        sendMessage('CACHE');
    }, 400);
    autosaveTimer.value = setTimeout(() => {
        submitSaveDraft(false);
    }, 2000);
}

const confirmPublish = () => {
    if (!title.value) {
        ElMessage.warning('请输入标题');
        return;
    }
    if (!publishForm.value.category) {
        ElMessage.warning('请选择分类');
        return;
    }
    publishDialogVisible.value = false;
    submitPublish();
};

const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    const wsUrl = formatWsUrl(API_CONFIG.baseURL);

    socketService = new SocketService(`${wsUrl}/api/article/ws`, token);

    socketService.onOpen(() => {
        console.log('已连接到服务器');
        if (articleId.value) {
            sendMessage('SELECT');
        }
    });

    socketService.onClose(() => {
        console.log('已断开与服务器的连接');
    });

    socketService.onError((error) => {
        console.log('错误: ' + error.message);
    });

    socketService.on('USER', (data) => console.log("用户消息-->>", data));
    socketService.on('SYSTEM', (data) => console.log("系统消息-->>", data));
    socketService.on('CACHE', () => {
        save.value = false;
        saveState.value = 'cached';
    });
    socketService.on('SAVE_DRAFT', (data) => {
        console.log("保存消息-->>", data);
        applyCommandResult(data, '草稿已保存');
    });
    socketService.on('PUBLISH', (data) => {
        console.log("发布消息-->>", data);
        applyCommandResult(data, '发布成功');
        router.push('/');
    });
    socketService.on('SELECT', (data) => {
        console.log("查询消息-->>", data);
        if (!data) {
            return;
        }
        hydrating.value = true;
        articleId.value = data.id || articleId.value;
        html.value = data.content || '';
        title.value = data.title || '';
        publishForm.value.category = data.category ?? null;
        publishForm.value.abstractText = data.abstractText || '';
        publishForm.value.cover = data.cover || '';
        lastSavedAt.value = data.updatedAt || null;
        saveState.value = data.status === 0 ? 'draft' : 'published';
        hasUnsavedChanges.value = false;
        if (editor.value) {
            editor.value.commands.setContent(data.content || '', false);
            wordCount.value = editor.value.storage.characterCount.characters();
        }
        hydrating.value = false;
    });

    socketService.connect();
};

const onInput = () => {
    queueSaveFlow();
};

watch(html, (newVal, oldVal) => {
    if (!hydrating.value && newVal !== oldVal) {
        queueSaveFlow();
    }
});

async function saveContent() {
    await submitSaveDraft(true);
}

async function publishContent() {
    publishDialogVisible.value = true;
}

const handleBeforeUnload = (e) => {
    if (hasUnsavedChanges.value) {
        e.preventDefault();
        e.returnValue = '';
    }
};

onMounted(() => {
    connectWebSocket();
    window.addEventListener('beforeunload', handleBeforeUnload);
});

onBeforeUnmount(() => {
    clearTimeout(cacheTimer.value);
    clearTimeout(autosaveTimer.value);
    if (editor.value) {
        editor.value.destroy();
    }
    window.removeEventListener('beforeunload', handleBeforeUnload);
    if (socketService) {
        socketService.close();
        socketService = null;
    }
});

onBeforeRouteUpdate((to, from) => {
    if (to.params.id !== from.params.id) {
        articleId.value = to.params.id ? Number(to.params.id) : null;
        if (to.params.id) {
            lastSavedAt.value = null;
            sendMessage('SELECT');
        }
    }
});
</script>

<style scoped>
.editor-layout {
    height: 100vh;
    background: var(--bg-color-base);
    /* Space for footer */
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.editor-container {
    flex: 1;
    display: flex;
    overflow: hidden;
    position: relative;
    max-width: 1400px;
    margin: 0 auto;
    width: 100%;
}

.scroll-area {
    flex: 1;
    overflow-y: auto;
    width: 100%;
    scroll-behavior: smooth;
}

.site-header {
    flex-shrink: 0;
}

.text-toolbar {
    position: relative;
    z-index: 100;
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-base);
    border-bottom: 1px solid var(--border-color-glass);
    transition: var(--transition-base);
}

.toolbar-wrapper {
    max-width: 1400px;
    margin: 0 auto;
    padding: 8px 24px;
    display: flex;
    flex-direction: row;
    /* Changed from column to row */
    align-items: center;
    justify-content: space-between;
    /* Add spacing */
    gap: 16px;
}

.status-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 12px;
    height: 20px;
    flex-shrink: 0;
    /* Prevent shrinking */
    padding-left: 16px;
    border-left: 1px solid rgba(0, 0, 0, 0.1);
}

.status-indicator {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background-color: #10b981;
    /* Green for saved */
    transition: all 0.3s;
}

.status-indicator.saving {
    background-color: #f59e0b;
    /* Amber for saving */
    box-shadow: 0 0 8px rgba(245, 158, 11, 0.5);
}

.status-text {
    color: #6b7280;
    font-size: 12px;
}

.glass-toolbar {
    border: none !important;
    background: transparent !important;
    /* width: 100%; Removed to allow status bar to fit */
    flex: 1;
}

.main-content {
    max-width: 900px;
    width: 100%;
    margin: 24px auto;
    padding: 60px 80px;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-base);
    min-height: calc(100vh - 200px);
    box-sizing: border-box;
}

.title-area {
    margin-bottom: 20px;
    border-bottom: 1px solid rgba(0, 0, 0, 0.05);
    padding-bottom: 12px;
}

.title-input {
    width: 100%;
    font-size: 28px;
    font-weight: 700;
    color: var(--text-color-primary);
    border: none;
    outline: none;
    background: transparent;
    padding: 0;
    font-family: inherit;
    line-height: 1.4;
}

.title-input::placeholder {
    color: var(--text-color-placeholder);
}

.main-content-editor {
    min-height: 60vh;
    /* Responsive height */
}

/* Tiptap Customization */
:deep(.ProseMirror) {
    outline: none;
    min-height: 60vh;
    font-size: 1.125rem;
    line-height: 1.8;
    color: var(--text-color-regular);
    padding-bottom: 40px;
}

:deep(.ProseMirror p.is-editor-empty:first-child::before) {
    color: #9ca3af;
    content: attr(data-placeholder);
    float: left;
    height: 0;
    pointer-events: none;
}

:deep(.ProseMirror img) {
    max-width: 100%;
    height: auto;
    border-radius: 8px;
    margin: 1.5rem 0;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

:deep(.ProseMirror blockquote) {
    border-left: 4px solid #3b82f6;
    padding-left: 1.5rem;
    margin-left: 0;
    margin-right: 0;
    color: #4b5563;
    font-style: italic;
    background: #f9fafb;
    padding: 1rem 1rem 1rem 1.5rem;
    border-radius: 0 8px 8px 0;
}

/* 代码块样式优化 */
:deep(.ProseMirror pre) {
    background: #1e293b;
    color: #f8fafc;
    padding: 1rem 1.5rem;
    border-radius: 8px;
    font-family: 'JetBrains Mono', Consolas, Monaco, monospace;
    overflow-x: auto;
    margin: 1.5rem 0;
    line-height: 1.6;
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}

/* 修复代码块内的 code 样式冲突 */
:deep(.ProseMirror pre code) {
    background: transparent;
    color: inherit;
    padding: 0;
    border-radius: 0;
    font-size: 0.9em;
    font-family: inherit;
}

/* 行内代码样式 */
:deep(.ProseMirror code:not(pre code)) {
    background: #f3f4f6;
    color: #ef4444;
    padding: 0.25rem 0.4rem;
    border-radius: 6px;
    font-family: Consolas, Monaco, monospace;
    font-size: 0.85em;
    font-weight: 500;
    border: 1px solid #e5e7eb;
}

/* Task List Styles */
:deep(ul[data-type="taskList"]) {
    list-style: none;
    padding: 0;
}

:deep(li[data-type="taskItem"]) {
    display: flex;
    flex-direction: row;
    align-items: flex-start;
    margin-bottom: 0.5rem;
}

:deep(li[data-type="taskItem"] label) {
    margin-right: 0.5rem;
    user-select: none;
}

:deep(li[data-type="taskItem"] > div) {
    flex: 1;
}

:deep(li[data-type="taskItem"] input[type="checkbox"]) {
    cursor: pointer;
}

.footer-card {
    position: fixed;
    bottom: 20px;
    right: 20px;
    width: auto;
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-base);
    padding: 10px 20px;
    border-radius: var(--border-radius-large);
    box-shadow: var(--box-shadow-hover);
    border: 1px solid var(--border-color-glass);
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    z-index: 100;
    gap: 12px;
    transition: var(--transition-base);
}

.footer-card:hover {
    background: rgba(255, 255, 255, 0.95);
}

.word-count {
    color: #9ca3af;
    font-size: 12px;
    white-space: nowrap;
}

.buttons {
    display: flex;
    gap: 8px;
}

.action-btn {
    padding: 6px 16px;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: all 0.2s ease;
}

.action-btn.save {
    background: transparent;
    color: #6b7280;
    border: 1px solid #e5e7eb;
}

.action-btn.save:hover {
    background: #f3f4f6;
    color: #374151;
}

.action-btn.publish {
    background: #3b82f6;
    color: white;
    box-shadow: 0 2px 6px rgba(59, 130, 246, 0.3);
}

.action-btn.publish:hover {
    background: #2563eb;
    box-shadow: 0 4px 10px rgba(37, 99, 235, 0.4);
}

/* Responsive */
@media (max-width: 1200px) {
    .main-content {
        max-width: 95%;
        padding: 30px;
    }
}
</style>
