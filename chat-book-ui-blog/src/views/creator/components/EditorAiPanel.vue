<template>
    <div class="ai-sidebar">
        <div class="ai-sidebar-header">
            <span class="ai-sidebar-title">AI 对话</span>
            <el-icon class="close-icon" @click="$emit('close')">
                <Close />
            </el-icon>
        </div>

        <div class="ai-sidebar-body">
            <div class="ai-placeholder">
                <p class="ai-placeholder-title">AI 对话区域预留</p>
                <p class="ai-placeholder-text">
                    后续可以在这里接入文档问答、选区改写、续写和智能补全。
                </p>
            </div>
        </div>
    </div>
</template>

<script setup>
import { Close } from '@element-plus/icons-vue';

defineEmits(['close']);
</script>

<style scoped>
.ai-sidebar {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    background:
        radial-gradient(circle at top right, rgba(59, 130, 246, 0.12), transparent 38%),
        linear-gradient(180deg, #ffffff 0%, #f8fbff 100%);
}

.ai-sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px 20px;
    border-bottom: 1px solid rgba(15, 23, 42, 0.08);
}

.ai-sidebar-title {
    font-weight: 600;
    color: var(--text-color-primary);
}

.close-icon {
    cursor: pointer;
    font-size: 18px;
    color: var(--text-color-regular);
    transition: color 0.2s ease;
}

.close-icon:hover {
    color: var(--text-color-primary);
}

.ai-sidebar-body {
    flex: 1;
    display: flex;
    align-items: center;
    justify-content: center;
    padding: 24px;
}

.ai-placeholder {
    width: 100%;
    max-width: 280px;
    padding: 24px 20px;
    border: 1px dashed rgba(59, 130, 246, 0.28);
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.86);
    box-shadow: 0 18px 40px rgba(148, 163, 184, 0.18);
}

.ai-placeholder-title {
    margin: 0 0 8px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.ai-placeholder-text {
    margin: 0;
    font-size: 13px;
    line-height: 1.7;
    color: var(--text-color-secondary);
}
</style>
