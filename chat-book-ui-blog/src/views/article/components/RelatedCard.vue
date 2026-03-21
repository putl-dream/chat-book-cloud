<template>
    <div class="related-articles-card">
        <h3 class="card-title">相关推荐</h3>
        <ul class="article-list" v-if="relatedArticles.length > 0">
            <li v-for="(article, index) in relatedArticles" :key="index" class="article-item" @click="openArticle(article.id)">
                <span class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</span>
                <span class="article-title" :title="article.title">{{ article.title }}</span>
            </li>
        </ul>
        <div v-else class="empty-state">
            <span>暂无相关推荐</span>
        </div>
    </div>
</template>

<script setup>
import { onMounted, toRef } from "vue";
import { useRelatedCard } from "../_hooks/useRelatedCard.js";

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    }
});

const articleIdRef = toRef(props, 'articleId');
const { relatedArticles, getRankClass, queryRelatedRequest, openArticle } = useRelatedCard(articleIdRef);

onMounted(() => {
    queryRelatedRequest();
});
</script>

<style scoped>
.related-articles-card {
    padding: 20px;
    background: var(--bg-color-white);
}

.card-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 20px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.card-title::before {
    content: '';
    width: 4px;
    height: 16px;
    background: var(--color-primary);
    border-radius: var(--border-radius-round);
}

.article-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.article-item {
    display: flex;
    align-items: center;
    padding: 8px 0;
    cursor: pointer;
    transition: var(--transition-fast);
    border-radius: var(--border-radius-base);
}

.article-item:hover {
    padding-left: 8px;
    background: var(--bg-color-light);
}

.article-item:hover .article-title {
    color: var(--color-primary);
}

.rank-badge {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 20px;
    height: 20px;
    border-radius: 6px;
    font-size: 11px;
    font-weight: 700;
    margin-right: 12px;
    flex-shrink: 0;
}

.rank-1 {
    background-color: #fee2e2;
    color: #ef4444;
}

.rank-2 {
    background-color: #fef3c7;
    color: #f59e0b;
}

.rank-3 {
    background-color: #ecfdf5;
    color: #10b981;
}

.rank-other {
    background-color: var(--border-color-light);
    color: var(--text-color-secondary);
}

.article-title {
    font-size: 0.875rem;
    color: var(--text-color-regular);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    flex: 1;
    transition: var(--transition-fast);
}

.empty-state {
    text-align: center;
    padding: 20px 0;
    color: var(--text-color-secondary);
    font-size: 0.875rem;
}
</style>
