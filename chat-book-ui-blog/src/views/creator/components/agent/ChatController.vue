<template>
    <div class="chat-controller">
        <div class="chat-header">
            <h3 class="chat-title">AI 创作助手</h3>
            <div class="chat-actions">
                <el-button 
                    class="ghost-action-btn" 
                    size="small" 
                    :disabled="!store.hasDraft"
                    @click="store.importDraftToEditor"
                >
                    导入编辑器继续
                </el-button>
                <el-button 
                    class="primary-action-btn" 
                    type="primary" 
                    size="small" 
                    :loading="store.generatingDraft"
                    :disabled="!store.hasMessages || store.chatting || store.hasDraft"
                    @click="store.createDraftFromSession"
                >
                    一键生成首稿
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
                <p class="placeholder-title">告诉 Agent 你的构想</p>
                <p class="placeholder-text">
                    可以输入文章主题、目标读者、情绪基调，以及你想要涵盖的几个核心观点。
                </p>
            </div>

            <div v-else class="chat-list custom-scrollbar-thin container-snap">
                <article 
                    v-for="msg in store.messages" 
                    :key="msg.id"
                    class="chat-msg"
                    :class="`msg-${msg.role}`"
                >
                    <div class="msg-bubble">
                        <p v-if="msg.streaming && !msg.content" class="streaming-hint">
                            <span class="typing-dot"></span> 正在思考...
                        </p>
                        <RichTextViewer
                            v-else
                            :html="renderHtml(msg.content)"
                            variant="chat" 
                            @click="handleMessageClick(msg, $event)"
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
                    placeholder="按 Ctrl+Enter 发送"
                    @keydown.ctrl.enter.prevent="handleSend"
                    :disabled="store.chatting || store.creatingSession || store.generatingDraft"
                />
                <div class="input-actions">
                    <el-button 
                        class="send-btn" 
                        type="primary" 
                        circle 
                        :loading="store.chatting || store.creatingSession"
                        :disabled="!inputValue.trim()"
                        @click="handleSend"
                    >
                        <el-icon><Position /></el-icon>
                    </el-button>
                </div>
            </div>
            <div class="footer-hint" v-if="store.hasDraft">
                💡 输入文本即向 Agent 下达<strong>局部优化重写</strong>指令
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue';
import { useAgentStudioStore } from '@/store/agentStudio.js';
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import { Position } from '@element-plus/icons-vue';

const store = useAgentStudioStore();
const inputValue = ref('');
const chatBodyRef = ref(null);

const handleSend = () => {
    if (!inputValue.value.trim() || store.chatting || store.loadingSession) return;
    
    const content = inputValue.value;
    inputValue.value = '';
    
    // If draft is already generated, we perform optimization instead of just chat
    if (store.hasDraft) {
        store.optimizeCurrentDraft(content);
    } else {
        store.sendMessage(content);
    }
};

const renderHtml = (content) => buildRichTextHtml(content || '', 'markdown');

const handleMessageClick = (msg, e) => {
    // Intercept clicks on bullet points/lists to act as quick-reply chips
    if (msg.role !== 'assistant') return;
    
    const li = e.target.closest('li');
    if (li && !store.chatting && !store.generatingDraft) {
        // Find raw text without HTML
        const textToSend = li.innerText.trim();
        if (textToSend) {
            store.sendMessage(textToSend);
        }
    }
};

// Auto scroll down when new message comes
watch(() => store.messages.length, async () => {
    await nextTick();
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
});
watch(() => store.messages[store.messages.length - 1]?.content, async () => {
    if (chatBodyRef.value) {
        chatBodyRef.value.scrollTop = chatBodyRef.value.scrollHeight;
    }
});
</script>

<style scoped>
.chat-controller {
    width: 360px;
    height: 100%;
    background: #fff;
    border-left: 1px solid rgba(22, 50, 79, 0.08);
    display: flex;
    flex-direction: column;
    z-index: 10;
    flex-shrink: 0;
}

.chat-header {
    height: 60px;
    padding: 0 16px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    border-bottom: 1px solid rgba(22, 50, 79, 0.05);
    background: rgba(255, 255, 255, 0.9);
    backdrop-filter: blur(10px);
}

.chat-title {
    margin: 0;
    font-size: 15px;
    font-weight: 600;
    color: #13273f;
    letter-spacing: -0.01em;
}

.chat-actions {
    display: flex;
    gap: 8px;
}

.primary-action-btn {
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    border: none;
    box-shadow: 0 4px 10px rgba(209, 96, 61, 0.2);
}

.ghost-action-btn {
    background: rgba(22, 50, 79, 0.04);
    border: none;
    color: #13273f;
}

.ghost-action-btn:hover {
    background: rgba(22, 50, 79, 0.08);
}

.chat-body {
    flex: 1;
    overflow-y: auto;
    padding: 8px;
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
    gap: 12px;
    padding-bottom: 10px;
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
    background: #fffbf5;
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

/* Agent Option Chips from Markdown Lists */
.msg-assistant .msg-bubble :deep(ul),
.msg-assistant .msg-bubble :deep(ol) {
    margin: 8px 0;
    padding-left: 0;
    list-style: none;
    display: flex;
    flex-direction: column;
    gap: 6px;
}

.msg-assistant .msg-bubble :deep(li) {
    background: #fff;
    border: 1px solid rgba(209, 96, 61, 0.15);
    padding: 6px 12px;
    border-radius: 8px;
    font-size: 13px;
    color: #d1603d;
    cursor: pointer;
    transition: all 0.2s ease;
    display: inline-block;
    width: fit-content;
}

.msg-assistant .msg-bubble :deep(li:hover) {
    background: rgba(209, 96, 61, 0.08);
    border-color: rgba(209, 96, 61, 0.4);
    transform: translateY(-1px);
    box-shadow: 0 4px 8px rgba(209, 96, 61, 0.08);
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
    padding: 12px;
    border-top: 1px solid rgba(22, 50, 79, 0.05);
    background: #fff;
}

.input-wrapper {
    position: relative;
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
    margin-top: 10px;
    padding: 0 4px;
}

@media (max-width: 900px) {
    .chat-controller {
        width: 100%;
        height: 50vh;
        border-left: none;
        border-top: 1px solid rgba(22, 50, 79, 0.08);
    }
}
</style>
