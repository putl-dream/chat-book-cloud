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
                    主题讨论、观点碰撞和知识点补全已经放到独立的 AI 创作页。先把问题聊透，再把初稿带回编辑器继续写。
                </p>
                <el-button class="ai-placeholder-action" type="primary" @click="router.push('/creative/agent')">
                    前往思考共创页
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
