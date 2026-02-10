<template>
    <div class="editor-layout">
        <CreativeHeader class="site-header" />

        <div class="text-toolbar">
            <div class="toolbar-wrapper">
                <TiptapToolbar :editor="editor" class="glass-toolbar" v-if="editor" />
                <div class="status-bar">
                    <div class="status-indicator" :class="{ 'saving': save }"></div>
                    <el-text class="status-text">{{ save ? '自动保存中...' : '文章已保存' }}</el-text>
                </div>
            </div>
        </div>

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

        <div class="footer-card">
            <div class="word-count">
                <el-text>共 {{ wordCount }} 字</el-text>
            </div>
            <div class="buttons">
                <button class="action-btn save" @click="saveContent">保存草稿</button>
                <button class="action-btn publish" @click="publishContent">发布文章</button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import CreativeHeader from "@/components/CreativeHeader.vue";
import TiptapToolbar from "@/components/TiptapToolbar.vue";
import { ElMessage } from "element-plus";
import { onBeforeRouteUpdate, useRoute } from "vue-router";
import SocketService from "@/utils/websocket.js";
import { API_CONFIG } from "@/config/index.js";

import { EditorContent, useEditor } from '@tiptap/vue-3';
import StarterKit from '@tiptap/starter-kit';
import Image from '@tiptap/extension-image';
import Placeholder from '@tiptap/extension-placeholder';
import CharacterCount from '@tiptap/extension-character-count';
import Underline from '@tiptap/extension-underline';
import TextAlign from '@tiptap/extension-text-align';
import Highlight from '@tiptap/extension-highlight';

// 初始化
let title = ref('');
const html = ref('');
const wordCount = ref(0);

// Tiptap Editor Initialization
const editor = useEditor({
    content: '',
    extensions: [
        StarterKit,
        Image,
        Underline,
        Highlight,
        Placeholder.configure({
            placeholder: '请输入内容...',
        }),
        CharacterCount,
        TextAlign.configure({
            types: ['heading', 'paragraph'],
        }),
    ],
    onUpdate: ({ editor }) => {
        const content = editor.getHTML();
        html.value = content;
        wordCount.value = editor.storage.characterCount.characters();
    },
    editorProps: {
        attributes: {
            class: 'prose prose-sm sm:prose lg:prose-lg xl:prose-2xl mx-auto focus:outline-none',
        },
    },
});

// WebSocket 相关
let socketService;
let debounceTimeout;
let save = ref(false);

const connectWebSocket = () => {
    const token = localStorage.getItem('token');
    // 构建 WebSocket URL
    let baseUrl = API_CONFIG.baseURL || 'http://localhost:8080';
    let wsUrl = baseUrl.replace(/^http/, 'ws').replace(/^https/, 'wss');

    socketService = new SocketService(`${wsUrl}/article/ws`, token);

    socketService.onOpen(() => {
        console.log('已连接到服务器');
        if (articleId && articleId !== '') {
            console.log("存在文章ID-->>", articleId, "正在查询")
            sendMessage('SELECT')
        }
    });

    socketService.onClose(() => {
        console.log('已断开与服务器的连接');
    });

    socketService.onError((error) => {
        console.log('错误: ' + error.message);
    });

    // 注册消息处理函数
    socketService.on('USER', (data) => console.log("用户消息-->>", data));
    socketService.on('SYSTEM', (data) => console.log("系统消息-->>", data));
    socketService.on('CACHE', () => save.value = false);
    socketService.on('SAVE', (data) => {
        console.log("保存消息-->>", data);
        ElMessage.success(data);
        window.location.href = '/';
    });
    socketService.on('PUBLISH', (data) => {
        console.log("发布消息-->>", data);
        ElMessage.success(data);
        window.location.href = '/';
    });
    socketService.on('SELECT', (data) => {
        console.log("查询消息-->>", data);
        html.value = data.content;
        title.value = data.title;
        // Sync editor content
        if (editor.value) {
            editor.value.commands.setContent(data.content);
            wordCount.value = editor.value.storage.characterCount.characters();
        }
    });

    socketService.connect();
}

// 监听标题输入事件
let typingTimer = null;
const onInput = () => {
    clearTimeout(typingTimer); // 清除之前的定时器
    typingTimer = setTimeout(() => {
        sendMessage('CACHE')
    }, 1500);
};

// 监听内容输入事件
watch(html, (newVal, oldVal) => {
    clearTimeout(typingTimer); // 清除之前的定时器
    if (newVal !== oldVal && newVal !== '') { // 如果输入框不为空
        save.value = true;
        typingTimer = setTimeout(() => {
            sendMessage('CACHE')
        }, 500);
    }
});

// 保存文章
async function saveContent() {
    ElMessage.success('文章正在保存为草稿！')
    setTimeout(() => {
        sendMessage('SAVE')
    }, 1000);

}

// 发布内容
async function publishContent() {
    // 等待1s
    ElMessage.success('文章正在发布中！')
    setTimeout(() => {
        sendMessage('PUBLISH')
    }, 1000);
}

const sendMessage = (type) => {
    const data = { id: articleId, title: title.value, content: html.value };
    if (socketService && socketService.isConnected()) {
        console.log("发送消息-->>", type, data);
        socketService.send(type, data);
    } else {
        console.log('WebSocket 未打开');
        // window.location.reload(); // 可选：是否重载页面
    }
};


// 获取路由参数
const route = useRoute();
const articleId = route.params.id;

// 绑定按钮点击事件
onMounted(() => {
    connectWebSocket();
});

onBeforeUnmount(() => {
    if (editor.value) {
        editor.value.destroy();
    }
    // Handle socket logic (same as original)
    saveContent();
});

// 监听路由变化
onBeforeRouteUpdate((to, from) => {
    if (to.params.id !== from.params.id) {
        console.log("路由变化-->>", to.params.id, from.params.id)
        window.location.reload();
    }
});

</script>

<style scoped>
.editor-layout {
    height: 100vh;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
    /* Space for footer */
    display: flex;
    flex-direction: column;
    overflow: hidden;
}

.scroll-area {
    flex: 1;
    overflow-y: auto;
    width: 100%;
}

.site-header {
    flex-shrink: 0;
}

.text-toolbar {
    position: relative;
    z-index: 100;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.5);
    box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
}

.toolbar-wrapper {
    max-width: 1200px;
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
    max-width: 1100px;
    /* Increased from 900px */
    width: 100%;
    margin: 24px auto;
    padding: 40px 50px;
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
    border-radius: 12px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.05);
    border: 1px solid rgba(255, 255, 255, 0.8);
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
    color: #1f2937;
    border: none;
    outline: none;
    background: transparent;
    padding: 0;
    font-family: inherit;
    line-height: 1.4;
}

.title-input::placeholder {
    color: #d1d5db;
}

.main-content-editor {
    min-height: 60vh;
    /* Responsive height */
}

/* Tiptap Customization */
:deep(.ProseMirror) {
    outline: none;
    min-height: 60vh;
    font-size: 17px;
    line-height: 1.8;
    color: #1f2937;
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

.footer-card {
    position: fixed;
    bottom: 20px;
    right: 20px;
    /* Moved to right corner */
    left: auto;
    transform: none;
    width: auto;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    padding: 10px 20px;
    border-radius: 12px;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
    border: 1px solid rgba(255, 255, 255, 0.5);
    display: flex;
    flex-direction: column;
    /* Stacked layout for compact corner view */
    align-items: flex-end;
    z-index: 100;
    gap: 12px;
    transition: opacity 0.3s;
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
