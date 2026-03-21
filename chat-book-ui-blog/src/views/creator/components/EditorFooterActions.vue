<template>
    <div class="footer-card glass-effect">
        <div class="word-count">
            <el-text>共 {{ wordCount }} 字</el-text>
        </div>
        <div class="buttons">
            <button type="button" class="action-btn save" @click="$emit('save')">保存草稿</button>
            <button type="button" class="action-btn publish" @click="$emit('publish')">发布文章</button>
        </div>
    </div>
</template>

<script setup>
defineProps({
    wordCount: {
        type: Number,
        default: 0,
    },
});

defineEmits(['save', 'publish']);
</script>

<style scoped>
.footer-card {
    position: fixed;
    right: 20px;
    bottom: 20px;
    z-index: 100;
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 12px;
    padding: 10px 20px;
    border-radius: var(--border-radius-large);
    transition: var(--transition-base);
}

.footer-card:hover {
    background: var(--bg-color-overlay);
}

.word-count {
    color: var(--text-color-placeholder);
    font-size: 12px;
    white-space: nowrap;
}

.buttons {
    display: flex;
    gap: 8px;
}

.action-btn {
    padding: 6px 16px;
    border: none;
    border-radius: 8px;
    font-size: 13px;
    font-weight: 500;
    cursor: pointer;
    transition: var(--transition-fast);
}

.action-btn.save {
    background: transparent;
    color: var(--text-color-secondary);
    border: 1px solid var(--border-color-base);
}

.action-btn.save:hover {
    background: var(--border-color-light);
    color: var(--text-color-primary);
}

.action-btn.publish {
    background: var(--color-primary);
    color: var(--bg-color-white);
    box-shadow: 0 2px 6px rgba(79, 70, 229, 0.25);
}

.action-btn.publish:hover {
    background: var(--color-primary-hover);
    box-shadow: 0 4px 10px rgba(79, 70, 229, 0.32);
}

@media (max-width: 768px) {
    .footer-card {
        right: 12px;
        bottom: 12px;
        padding: 10px 12px;
    }

    .buttons {
        width: 100%;
    }

    .action-btn {
        flex: 1;
    }
}
</style>
