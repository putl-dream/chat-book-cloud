<template>
    <div class="default-sidebar">
        <!-- 点击文章头部作者名时，右侧切换为此 default 模块（作者卡片 + 热门） -->
        <div class="article-right-card">
            <AuthorCard :userId="userId" />
        </div>
        <div class="article-right-card">
            <HotCard />
        </div>
    </div>
</template>

<script setup>
import AuthorCard from "@/components/domain/AuthorCard.vue";
import HotCard from "@/components/domain/HotCard.vue";

defineProps({
    userId: {
        type: [Number, String],
        required: false
    }
});
</script>

<style scoped>
.default-sidebar {
    display: flex;
    flex-direction: column;
    gap: 12px;
    height: 100%;
    overflow-y: auto;
    padding-right: 4px;
    /* 防止滚动条遮挡内容 */
}

/* 隐藏滚动条但保留功能 (Chrome/Safari/Webkit) */
.default-sidebar::-webkit-scrollbar {
    width: 4px;
}

.default-sidebar::-webkit-scrollbar-track {
    background: transparent;
}

.default-sidebar::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 4px;
}

.default-sidebar::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
}

.article-right-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    overflow: hidden;
    flex-shrink: 0;
    /* 防止卡片被压缩 */
}
</style>
