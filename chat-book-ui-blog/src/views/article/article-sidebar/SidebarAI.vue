<template>
    <div class="panel-content ai-panel">
        <div class="panel-header">
            <el-text size="large"><b>AI 助手</b></el-text>
        </div>
        <div class="ai-chat-history">
            <div v-for="(msg, idx) in aiChatHistory" :key="idx" class="ai-msg" :class="msg.role">
                <div class="msg-content">
                    <RichTextViewer
                        :content="msg.content"
                        source-format="markdown"
                        variant="chat" />
                </div>
            </div>
        </div>
        <div class="ai-input-area">
            <el-input v-model="aiMessage" placeholder="向AI讨论..." @keyup.enter="sendAiMessage">
                <template #append>
                    <el-button @click="sendAiMessage">发送</el-button>
                </template>
            </el-input>
        </div>
    </div>
</template>

<script setup>
import { ElInput, ElButton, ElText } from 'element-plus';
import RichTextViewer from "@/components/common/rich-text/RichTextViewer.vue";
import { useSidebarAI } from "../_hooks/useSidebarAI.js";

const { aiMessage, aiChatHistory, sendAiMessage } = useSidebarAI();
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
    padding: 10px 14px;
    border-radius: 14px;
    max-width: 80%;
    word-break: break-word;
    min-width: 0;
}

.ai-msg.user .msg-content {
    background-color: #ecf5ff;
    color: #409eff;
}

.ai-msg.ai .msg-content {
    background-color: #f4f4f5;
    color: #606266;
}

.msg-content :deep(.rich-text-viewer__body) {
    color: inherit;
}

.ai-input-area {
    padding: 16px;
    border-top: 1px solid var(--border-color-light);
    flex-shrink: 0;
}
</style>
