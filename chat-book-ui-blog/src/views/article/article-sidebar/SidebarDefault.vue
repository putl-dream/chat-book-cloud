<template>
    <div class="default-sidebar">
        <div class="article-right-card toc-card">
            <ArticleViewerToc :articleHtml="articleHtml" :contentTarget="contentTarget" />
        </div>
        <div class="article-right-card">
            <ArticleTagCard :articleId="articleId" :tagIds="tagIds" />
        </div>
        <div class="article-right-card">
            <RelatedCard :articleId="articleId" />
        </div>
    </div>
</template>

<script setup>
import ArticleViewerToc from "@/views/article/components/ArticleViewerToc.vue";
import ArticleTagCard from "@/views/article/components/ArticleTagCard.vue";
import RelatedCard from "@/views/article/components/RelatedCard.vue";

defineProps({
    articleId: {
        type: [Number, String],
        required: true
    },
    tagIds: {
        type: Array,
        default: () => []
    },
    articleHtml: {
        type: String,
        default: ''
    },
    contentTarget: {
        type: Object,
        default: null
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
    /* 防止滚动条遮挡内?*/
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

.toc-card {
    min-height: 240px;
}

@media (max-width: 768px) {
    .article-right-card:not(.toc-card) {
        display: none !important;
    }
}
</style>
