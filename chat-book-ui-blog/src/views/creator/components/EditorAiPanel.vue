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
                <p class="ai-placeholder-title c-editor-panel__placeholder-title">AI 对话区域预留</p>
                <p class="ai-placeholder-text c-editor-panel__placeholder-text">
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
