<template>
    <div class="panel-content ai-panel">
        <div class="panel-header">
            <el-text size="large"><b>AI 助手</b></el-text>
        </div>
        <div class="ai-chat-history">
            <div v-for="(msg, idx) in aiChatHistory" :key="idx" class="ai-msg" :class="msg.role">
                <div class="msg-content">
                    <MarkdownRenderer :content="msg.content" />
                </div>
            </div>
        </div>
        <div class="ai-input-area">
            <el-input v-model="aiMessage" placeholder="与 AI 讨论..." @keyup.enter="sendAiMessage">
                <template #append>
                    <el-button @click="sendAiMessage">发送</el-button>
                </template>
            </el-input>
        </div>
    </div>
</template>

<script setup>
import { ref } from 'vue';
import { ElInput, ElButton, ElText } from 'element-plus';
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";

const aiMessage = ref('');
const aiChatHistory = ref([
    { role: 'ai', content: '你好！我是这篇文章的AI助手，有什么可以帮你的吗？' }
]);

const sendAiMessage = () => {
    if (!aiMessage.value.trim()) return;
    aiChatHistory.value.push({ role: 'user', content: aiMessage.value });
    const userMsg = aiMessage.value;
    aiMessage.value = '';

    // 模拟AI回复
    setTimeout(() => {
        aiChatHistory.value.push({ role: 'ai', content: `针对"${userMsg}"，我觉得这篇文章写得很有深度... (AI功能开发中)` });
    }, 1000);
};
</script>

<style scoped>
.panel-content {
    display: flex;
    flex-direction: column;
    height: 100%;
    overflow: hidden;
}

.panel-header {
    padding: 16px;
    border-bottom: 1px solid var(--border-color-light);
    display: flex;
    justify-content: space-between;
    align-items: center;
    background: #fff;
    flex-shrink: 0;
}

.ai-chat-history {
    flex: 1;
    overflow-y: auto;
    padding: 16px;
    display: flex;
    flex-direction: column;
    gap: 12px;
}

.ai-msg {
    display: flex;
    margin-bottom: 8px;
}

.ai-msg.user {
    justify-content: flex-end;
}

.ai-msg.ai {
    justify-content: flex-start;
}

.msg-content {
    padding: 8px 12px;
    border-radius: 8px;
    max-width: 80%;
    word-break: break-word;
    font-size: 0.9rem;
    line-height: 1.5;
}

.ai-msg.user .msg-content {
    background-color: #ecf5ff;
    color: #409eff;
}

.ai-msg.ai .msg-content {
    background-color: #f4f4f5;
    color: #606266;
}

.ai-input-area {
    padding: 16px;
    border-top: 1px solid var(--border-color-light);
    flex-shrink: 0;
}
</style>
