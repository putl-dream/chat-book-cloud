<template>
    <div class="chat-controller">
        <div class="chat-header">
            <div class="chat-heading">
                <div class="scene-line">
                    <span class="scene-badge">{{ store.currentSceneLabel }}</span>
                    <span v-if="store.nextSceneLabel && store.nextSceneLabel !== store.currentSceneLabel" class="scene-next">
                        下一阶段：{{ store.nextSceneLabel }}
                    </span>
                </div>
                <h3 class="chat-title">{{ store.currentSceneLabel }}</h3>
                <p class="chat-subtitle">{{ store.currentSceneSubtitle }}</p>
                <p v-if="store.switchReason" class="chat-reason">{{ store.switchReason }}</p>
            </div>
            <div class="chat-actions">
                <el-button 
                    class="primary-action-btn" 
                    type="primary" 
                    size="small" 
                    :loading="store.generatingDraft"
                    :disabled="!store.hasMessages || store.chatting || store.loadingSession || store.hasPendingInteractiveForm || (!store.isDraftReady && store.currentScene !== 'EDIT')"
                    @click="store.createDraftFromSession"
                >
                    {{ store.generateButtonLabel }}
                </el-button>
            </div>
        </div>

        <div class="chat-body custom-scrollbar" ref="chatBodyRef" @scroll="handleChatScroll">
            <div 
                v-if="store.loadingSession" 
                class="chat-placeholder"
            >
                <el-skeleton :rows="6" animated />
            </div>

            <div 
                v-else-if="!store.hasMessages" 
                class="chat-placeholder u-animate-fade-in"
            >
                <p class="placeholder-title">先把你脑中的问题抛出来</p>
                <p class="placeholder-text">
                    输入主题、观点、困惑或预设立场。Agent 会帮你扩展知识点、识别盲点、补充反方视角，而不是直接替你写整篇文章。
                </p>
            </div>

            <div v-else class="chat-list custom-scrollbar-thin container-snap">
                <article 
                    v-for="msg in store.visibleMessages" 
                    :key="msg.id"
                    class="chat-msg"
                    :class="`msg-${msg.role}`"
                >
                    <InteractiveFormCard
                        v-if="msg.role === 'assistant' && msg.messageType === 'interactive_form'"
                        class="msg-interactive-card"
                        :message="msg"
                        :disabled="store.chatting || store.generatingDraft"
                        @submit="handleInteractiveSubmit(msg, $event)"
                    />
                    <div v-else class="msg-bubble">
                        <p v-if="msg.streaming && !msg.content" class="streaming-hint">
                            <span class="typing-dot"></span> 正在思考...
                        </p>
                        <StreamingMessageRenderer
                            v-else-if="msg.role === 'assistant' && msg.messageType === 'text'"
                            :content="msg.previewText || msg.content"
                            :streaming="msg.streaming && !store.chatPreviewSettled"
                        />
                        <RichTextViewer
                            v-else
                            :html="renderHtml(msg.content)"
                            variant="chat"
                        />
                    </div>
                </article>
            </div>
        </div>

        <div class="chat-footer">
            <div class="input-wrapper">
                <el-input
                    v-model="inputValue"
                    type="textarea"
                    resize="none"
                    :autosize="{ minRows: 1, maxRows: 6 }"
                    maxlength="1200"
                    :placeholder="inputPlaceholder"
                    @keydown.ctrl.enter.prevent="handleSend"
                    :disabled="store.loadingSession || store.creatingSession || store.generatingDraft || store.hasPendingInteractiveForm"
                />
                <div class="input-actions">
                    <el-button
                        v-if="store.chatting"
                        class="interrupt-btn"
                        text
                        :loading="store.interruptingChat"
                        @click="handleInterrupt"
                    >
                        打断回复
                    </el-button>
                    <el-button 
                        class="send-btn" 
                        type="primary" 
                        circle 
                        :loading="store.creatingSession"
                        :disabled="store.chatting || store.interruptingChat || store.loadingSession || !inputValue.trim() || store.hasPendingInteractiveForm"
                        @click="handleSend"
                    >
                        <el-icon><Position /></el-icon>
                    </el-button>
                </div>
            </div>
            <div class="footer-hint" v-if="store.hasPendingInteractiveForm">
                请先完成上方问题卡片，Agent 会在收到完整答案后继续生成建议
            </div>
            <div class="footer-hint" v-else-if="store.chatting && store.chatPreviewSettled">
                可见回复已完成，正在同步最终结果。你现在看到的内容基本已经稳定。
            </div>
            <div class="footer-hint" v-else-if="store.chatting">
                当前回复进行中。你可以先继续修改输入内容；如果上一条发早了，点“打断回复”后再发送。
            </div>
            <div class="footer-hint" v-else>
                {{ store.sceneFooterHint }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, ref, watch, nextTick, onBeforeUnmount } from 'vue';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import InteractiveFormCard from '@/views/creator/components/agent/InteractiveFormCard.vue';
import StreamingMessageRenderer from '@/views/creator/components/agent/StreamingMessageRenderer.vue';
import { Position } from '@element-plus/icons-vue';

const store = useAgentStudioStore();
const inputValue = ref('');
const chatBodyRef = ref(null);
const shouldAutoFollow = ref(true);
let scheduledScrollTimer = null;
const AUTO_SCROLL_THRESHOLD_PX = 40;
const AUTO_SCROLL_INTERVAL_MS = 64;
const inputPlaceholder = computed(() => (
    store.hasPendingInteractiveForm
        ? '请先完成上方问题卡片'
        : store.chatting
            ? '可先修改输入内容，打断当前回复后重新发送'
            : '按 Ctrl+Enter 发送'
));

const handleSend = () => {
    if (!inputValue.value.trim() || store.chatting || store.interruptingChat || store.loadingSession) return;
    
    const content = inputValue.value;
    inputValue.value = '';

    store.sendMessage(content);
};

const handleInterrupt = async () => {
    if (!inputValue.value.trim() && store.latestUserEditableMessage) {
        inputValue.value = store.latestUserEditableMessage;
    }
    await store.interruptChat();
};

const renderHtml = (content) => buildRichTextHtml(content || '', 'markdown');
const handleInteractiveSubmit = (message, answers) => store.submitInteractiveForm(message, answers);

const cancelScheduledAutoScroll = () => {
    if (scheduledScrollTimer == null) {
        return;
    }
    clearTimeout(scheduledScrollTimer);
    scheduledScrollTimer = null;
};

const syncAutoFollowState = () => {
    if (!chatBodyRef.value) {
        shouldAutoFollow.value = true;
        return;
    }
    const { scrollHeight, scrollTop, clientHeight } = chatBodyRef.value;
    shouldAutoFollow.value = scrollHeight - scrollTop - clientHeight <= AUTO_SCROLL_THRESHOLD_PX;
};

const scrollToBottom = () => {
    if (!chatBodyRef.value) {
        return;
    }
    chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    shouldAutoFollow.value = true;
};

const scheduleAutoScroll = ({ force = false } = {}) => {
    if (!chatBodyRef.value || (!force && !shouldAutoFollow.value)) {
        return;
    }
    if (scheduledScrollTimer != null) {
        return;
    }
    scheduledScrollTimer = setTimeout(() => {
        scheduledScrollTimer = null;
        scrollToBottom();
    }, AUTO_SCROLL_INTERVAL_MS);
};

const handleChatScroll = () => {
    syncAutoFollowState();
};

watch(() => {
    const lastMessage = store.visibleMessages[store.visibleMessages.length - 1];
    return [
        store.visibleMessages.length,
        lastMessage?.id ?? '',
        lastMessage?.content ?? '',
        lastMessage?.previewText ?? ''
    ];
}, async () => {
    await nextTick();
    scheduleAutoScroll();
}, {
    flush: 'post'
});

onBeforeUnmount(() => {
    cancelScheduledAutoScroll();
});
</script>

<style scoped>
.chat-controller {
    flex: 1;
    height: 100%;
    min-width: 0;
    background:
        radial-gradient(circle at top left, rgba(var(--color-primary-rgb), 0.08), transparent 26%),
        linear-gradient(180deg, var(--bg-color-glass), var(--bg-color-base));
    display: flex;
    flex-direction: column;
    z-index: 10;
}

.chat-header {
    min-height: 88px;
    padding: 18px 22px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid var(--border-color-light);
    background: var(--bg-color-overlay);
    backdrop-filter: blur(16px);
    gap: 16px;
}

.chat-heading {
    display: flex;
    flex-direction: column;
    gap: 6px;
    min-width: 0;
}

.scene-line {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
}

.scene-badge {
    display: inline-flex;
    align-items: center;
    padding: 4px 10px;
    border-radius: 999px;
    background: var(--color-primary-light);
    color: var(--color-primary);
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.04em;
}

.scene-next {
    font-size: 11px;
    color: var(--text-color-secondary);
}

.chat-title {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: var(--text-color-primary);
    letter-spacing: -0.02em;
}

.chat-subtitle {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: var(--text-color-regular);
}

.chat-reason {
    margin: 0;
    font-size: 12px;
    line-height: 1.4;
    color: var(--text-color-secondary);
}

.chat-actions {
    display: flex;
    gap: 8px;
    flex-shrink: 0;
}

.chat-body {
    flex: 1;
    overflow-y: auto;
    padding: 16px 22px 8px;
    display: flex;
    flex-direction: column;
    scroll-behavior: smooth;
}

.chat-placeholder {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    text-align: center;
    color: var(--text-color-secondary);
    padding: 20px;
    max-width: 540px;
    margin: 0 auto;
}

.placeholder-title {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin: 0 0 8px;
}

.placeholder-text {
    font-size: 13px;
    line-height: 1.6;
    margin: 0;
}

.chat-list {
    display: flex;
    flex-direction: column;
    gap: 14px;
    padding-bottom: 10px;
    max-width: 920px;
    width: 100%;
    margin: 0 auto;
}

.chat-msg {
    display: flex;
    gap: 10px;
    align-items: flex-start;
}

.msg-user {
    flex-direction: row-reverse;
}

.msg-system {
    justify-content: center;
}

.msg-avatar {
    width: 36px;
    height: 36px;
    border-radius: 12px;
    background: var(--bg-color-hover);
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    color: var(--text-color-regular);
}

.msg-assistant .msg-avatar {
    background: var(--color-primary-light);
    color: var(--color-primary);
}

.msg-user .msg-avatar {
    background: var(--color-primary);
    color: #fff;
}

.msg-system .msg-avatar {
    display: none;
}

.msg-bubble {
    padding: 2px 8px;
    border-radius: 14px;
    background: var(--bg-color-white);
    border: 1px solid var(--border-color-light);
    font-size: 14px;
    line-height: 1.5;
    color: var(--text-color-primary);
    word-break: break-word;
    overflow-x: auto;
}

.msg-user .msg-bubble {
    background: var(--color-primary);
    color: #fff;
    border: none;
    border-top-right-radius: 4px;
}

.msg-user .msg-bubble :deep(*) {
    color: #ffffff !important;
}

.msg-assistant .msg-bubble {
    border-top-left-radius: 4px;
    background: var(--bg-color-white);
    box-shadow: var(--box-shadow-base);
    border-color: var(--border-color-light);
}

.msg-system .msg-bubble {
    background: var(--color-primary-light);
    color: var(--color-primary);
    border: none;
    font-size: 12px;
    padding: 8px 16px;
    border-radius: 999px;
    text-align: center;
    max-width: 100%;
}

.msg-interactive-card {
    width: 100%;
}

.streaming-hint {
    margin: 0;
    color: var(--text-color-secondary);
    display: flex;
    align-items: center;
    gap: 6px;
}

.typing-dot {
    width: 6px;
    height: 6px;
    background: currentColor;
    border-radius: 50%;
    animation: typing 1s infinite alternate ease-in-out;
}

@keyframes typing {
    from { opacity: 0.3; transform: scale(0.8); }
    to { opacity: 1; transform: scale(1.2); }
}

.chat-footer {
    padding: 16px 22px 18px;
    border-top: 1px solid var(--border-color-light);
    background: var(--bg-color-overlay);
    backdrop-filter: blur(16px);
}

.input-wrapper {
    position: relative;
    max-width: 920px;
    margin: 0 auto;
    border: 1px solid var(--border-color-base);
    border-radius: 12px;
    transition: all 0.2s ease;
    background: var(--bg-color-white);
    display: flex;
    flex-direction: column;
    padding: 6px;
}

.input-wrapper:focus-within {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 2px rgba(var(--color-primary-rgb), 0.15);
}

.input-wrapper :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: none !important;
    background: transparent !important;
    padding: 4px 6px;
    font-size: 14px;
    color: var(--text-color-primary);
    line-height: 1.5;
}

.input-actions {
    display: flex;
    justify-content: flex-end;
    align-items: center;
    gap: 8px;
    margin-top: 4px;
}

.interrupt-btn {
    color: var(--text-color-regular);
}

.send-btn {
    width: 28px;
    height: 28px;
    padding: 0;
    transition: all 0.2s ease;
    background: var(--color-primary);
    border: none;
}

.send-btn:hover:not(:disabled) {
    background: var(--color-primary-hover);
    transform: scale(1.05);
}

.send-btn:disabled {
    background: var(--border-color-light);
    color: var(--text-color-placeholder);
}

.footer-hint {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin: 10px auto 0;
    max-width: 920px;
    padding: 0 4px;
    text-align: left;
}

.primary-action-btn {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-hover));
    border: none;
    box-shadow: 0 4px 10px rgba(var(--color-primary-rgb), 0.2);
}

@media (max-width: 900px) {
    .chat-controller {
        width: 100%;
        min-height: calc(100vh - 220px);
    }

    .chat-header {
        flex-direction: column;
        align-items: stretch;
    }

    .chat-actions {
        justify-content: flex-start;
    }
}
</style>
