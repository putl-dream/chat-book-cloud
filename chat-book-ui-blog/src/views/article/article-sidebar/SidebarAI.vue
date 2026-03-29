<template>
    <div class="panel-content ai-panel c-sidebar-panel">
        <div class="panel-header c-sidebar-panel__header">
            <el-text size="large"><b>AI 助手</b></el-text>
        </div>
        <div class="ai-chat-history c-sidebar-panel__body">
            <div v-for="(msg, idx) in aiChatHistory" :key="idx" class="ai-msg" :class="msg.role">
                <div class="msg-content">
                    <RichTextViewer
                        :html="renderMessageHtml(msg)"
                        variant="chat" />
                </div>
            </div>
        </div>
        <div class="ai-input-area c-sidebar-panel__footer">
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
import { buildRichTextHtml } from "@/components/common/rich-text/content-pipeline.js";
import { useSidebarAI } from "../_hooks/useSidebarAI.js";

const { aiMessage, aiChatHistory, sendAiMessage } = useSidebarAI();

const renderMessageHtml = (message) => buildRichTextHtml(message?.content || '', message?.sourceFormat || 'markdown');
</script>

<style scoped>
.panel-content {
    --sidebar-panel-header-padding: 16px;
    --sidebar-panel-footer-padding: 16px;
}

.panel-header {
    min-height: 56px;
}

.ai-chat-history {
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
}
</style>
