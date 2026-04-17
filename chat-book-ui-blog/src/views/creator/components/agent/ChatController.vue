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

        <div class="chat-body custom-scrollbar" ref="chatBodyRef">
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
                            v-else-if="msg.streaming"
                            :content="msg.content"
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
                    :disabled="store.chatting || store.creatingSession || store.generatingDraft || store.hasPendingInteractiveForm"
                />
                <div class="input-actions">
                    <el-button 
                        class="send-btn" 
                        type="primary" 
                        circle 
                        :loading="store.chatting || store.creatingSession"
                        :disabled="!inputValue.trim() || store.hasPendingInteractiveForm"
                        @click="handleSend"
                    >
                        <el-icon><Position /></el-icon>
                    </el-button>
                </div>
            </div>
            <div class="footer-hint" v-if="store.hasPendingInteractiveForm">
                请先完成上方问题卡片，Agent 会在收到完整答案后继续生成建议
            </div>
            <div class="footer-hint" v-else>
                {{ store.sceneFooterHint }}
            </div>
        </div>
    </div>
</template>

<script setup>
import { computed, ref, watch, nextTick } from 'vue';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import InteractiveFormCard from '@/views/creator/components/agent/InteractiveFormCard.vue';
import StreamingMessageRenderer from '@/views/creator/components/agent/StreamingMessageRenderer.vue';
import { Position } from '@element-plus/icons-vue';

const store = useAgentStudioStore();
const inputValue = ref('');
const chatBodyRef = ref(null);
const inputPlaceholder = computed(() => (
    store.hasPendingInteractiveForm
        ? '请先完成上方问题卡片'
        : '按 Ctrl+Enter 发送'
));

const handleSend = () => {
    if (!inputValue.value.trim() || store.chatting || store.loadingSession) return;
    
    const content = inputValue.value;
    inputValue.value = '';

    store.sendMessage(content);
};

const renderHtml = (content) => buildRichTextHtml(content || '', 'markdown');
const handleInteractiveSubmit = (message, answers) => store.submitInteractiveForm(message, answers);

// Auto scroll down when new message comes
watch(() => store.visibleMessages.length, async () => {
    await nextTick();
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
});
watch(() => store.visibleMessages[store.visibleMessages.length - 1]?.content, async () => {
    await nextTick();
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
});
</script>

<style scoped>
.chat-controller {
    flex: 1;
    height: 100%;
    min-width: 0;
    background:
        radial-gradient(circle at top left, rgba(209, 96, 61, 0.08), transparent 26%),
        linear-gradient(180deg, rgba(255, 251, 247, 0.86), rgba(255, 255, 255, 0.98));
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
    border-bottom: 1px solid rgba(22, 50, 79, 0.05);
    background: rgba(255, 255, 255, 0.8);
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
    background: rgba(209, 96, 61, 0.12);
    color: #b24a28;
    font-size: 11px;
    font-weight: 700;
    letter-spacing: 0.04em;
}

.scene-next {
    font-size: 11px;
    color: rgba(19, 39, 63, 0.52);
}

.chat-title {
    margin: 0;
    font-size: 18px;
    font-weight: 700;
    color: #13273f;
    letter-spacing: -0.02em;
}

.chat-subtitle {
    margin: 0;
    font-size: 13px;
    line-height: 1.5;
    color: rgba(19, 39, 63, 0.58);
}

.chat-reason {
    margin: 0;
    font-size: 12px;
    line-height: 1.4;
    color: rgba(19, 39, 63, 0.46);
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
    color: rgba(19, 39, 63, 0.6);
    padding: 20px;
    max-width: 540px;
    margin: 0 auto;
}

.placeholder-title {
    font-size: 16px;
    font-weight: 600;
    color: #13273f;
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
    background: rgba(22, 50, 79, 0.06);
    flex-shrink: 0;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 12px;
    font-weight: 600;
    color: rgba(19, 39, 63, 0.7);
}

.msg-assistant .msg-avatar {
    background: rgba(209, 96, 61, 0.1);
    color: #d1603d;
}

.msg-user .msg-avatar {
    background: rgba(22, 50, 79, 0.8);
    color: #fff;
}

.msg-system .msg-avatar {
    display: none;
}

.msg-bubble {
    padding: 2px 8px;
    border-radius: 14px;
    background: #F9FAFB;
    border: 1px solid rgba(22, 50, 79, 0.04);
    font-size: 14px;
    line-height: 1.5;
    color: #13273f;
    word-break: break-word;
    overflow-x: auto;
}

.msg-user .msg-bubble {
    background: #13273f;
    color: #fff;
    border: none;
    border-top-right-radius: 4px;
}

.msg-user .msg-bubble :deep(*) {
    color: #ffffff !important;
}

.msg-assistant .msg-bubble {
    border-top-left-radius: 4px;
    background: rgb(240, 244, 249);
    box-shadow: 0 2px 8px rgba(21, 37, 64, 0.04);
    border-color: rgba(22, 50, 79, 0.08);
}

.msg-system .msg-bubble {
    background: rgba(209, 96, 61, 0.06);
    color: rgba(209, 96, 61, 0.8);
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
    color: rgba(19, 39, 63, 0.5);
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
    border-top: 1px solid rgba(22, 50, 79, 0.05);
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(16px);
}

.input-wrapper {
    position: relative;
    max-width: 920px;
    margin: 0 auto;
    border: 1px solid rgba(22, 50, 79, 0.15);
    border-radius: 12px;
    transition: all 0.2s ease;
    background: #fff;
    display: flex;
    flex-direction: column;
    padding: 6px;
}

.input-wrapper:focus-within {
    border-color: rgba(209, 96, 61, 0.5);
    box-shadow: 0 0 0 2px rgba(209, 96, 61, 0.08);
}

.input-wrapper :deep(.el-textarea__inner) {
    box-shadow: none !important;
    border: none !important;
    background: transparent !important;
    padding: 4px 6px;
    font-size: 14px;
    color: #13273f;
    line-height: 1.5;
}

.input-actions {
    display: flex;
    justify-content: flex-end;
    margin-top: 4px;
}

.send-btn {
    width: 28px;
    height: 28px;
    padding: 0;
    transition: all 0.2s ease;
    background: #d1603d;
    border: none;
}

.send-btn:hover:not(:disabled) {
    background: #b94725;
    transform: scale(1.05);
}

.send-btn:disabled {
    background: rgba(22, 50, 79, 0.1);
    color: rgba(255, 255, 255, 0.9);
}

.footer-hint {
    font-size: 12px;
    color: rgba(19, 39, 63, 0.5);
    margin: 10px auto 0;
    max-width: 920px;
    padding: 0 4px;
    text-align: left;
}

.primary-action-btn {
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    border: none;
    box-shadow: 0 4px 10px rgba(209, 96, 61, 0.2);
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
