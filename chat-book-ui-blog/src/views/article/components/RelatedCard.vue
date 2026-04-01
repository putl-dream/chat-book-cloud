<template>
    <div class="related-articles-card c-solid-panel c-solid-panel--compact">
        <h3 class="card-title c-panel-title c-panel-title--compact">相关推荐</h3>
        <ul class="article-list c-article-rank-list" v-if="relatedArticles.length > 0">
            <li v-for="(article, index) in relatedArticles" :key="index" class="article-item c-article-rank-list__item" @click="openArticle(article.id)">
                <span class="rank-badge c-article-rank-list__badge" :class="getRankClass(index)">{{ index + 1 }}</span>
                <span class="article-title c-article-rank-list__title" :title="article.title">{{ article.title }}</span>
            </li>
        </ul>
        <div v-else class="empty-state c-article-rank-card__empty c-empty-panel c-empty-panel--plain c-empty-panel--compact">
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
