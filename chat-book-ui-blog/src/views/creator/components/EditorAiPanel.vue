<template>
    <div class="ai-sidebar c-editor-panel c-editor-panel--ai">
        <div class="ai-sidebar-header c-editor-panel__header">
            <span class="ai-sidebar-title c-editor-panel__title">AI 对话</span>
            <el-icon class="close-icon c-editor-panel__close" @click="$emit('close')">
                <Close />
            </el-icon>
        </div>

        <div class="ai-sidebar-body c-editor-panel__body c-editor-panel__body--center">
            <div class="ai-placeholder c-editor-panel__placeholder">
                <p class="ai-placeholder-title c-editor-panel__placeholder-title">Agent Studio 已接入</p>
                <p class="ai-placeholder-text c-editor-panel__placeholder-text">
                    会话建联、首稿生成、候选优化和版本对比已经放到独立的 AI 创作页，适合在正式编辑前先把文章结构聊清楚。
                </p>
                <el-button class="ai-placeholder-action" type="primary" @click="router.push('/creative/agent')">
                    前往 AI 创作页
                </el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import { useRouter } from 'vue-router';
import { Close } from '@element-plus/icons-vue';

defineEmits(['close']);
const router = useRouter();
</script>

<style scoped>
.ai-placeholder-action {
    margin-top: 18px;
    width: 100%;
    border: none;
    border-radius: 999px;
    background: linear-gradient(135deg, #d1603d, #c04e2b);
    box-shadow: 0 14px 28px rgba(209, 96, 61, 0.22);
}
</style>
