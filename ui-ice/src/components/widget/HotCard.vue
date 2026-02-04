<template>
    <div class="hot-articles-card">
        <h3 class="card-title">热门文章</h3>
        <ul class="article-list">
            <li v-for="(article, index) in hotArticles" :key="index" class="article-item" @click="openArticle(article.id)">
                <span class="rank-badge" :class="getRankClass(index)">{{ index + 1 }}</span>
                <span class="article-title" :title="article.title">{{ article.title }}</span>
            </li>
        </ul>
    </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {getHotPage} from "@/api/article.js";
import router from "@/router/index.js";

const hotArticles = ref([]);

const getRankClass = (index) => {
    if (index === 0) return 'rank-1';
    if (index === 1) return 'rank-2';
    if (index === 2) return 'rank-3';
    return 'rank-other';
};

const queryHotRequest = async () => {
    try {
        const response = await getHotPage(1, 10);
        hotArticles.value = response.data.records;
    } catch (error) {
        console.error('Failed to fetch hot articles:', error);
    }
};

const openArticle = async (id) => {
    await router.push({name: 'Article', params: {id: id}});
};

onMounted(() => {
    queryHotRequest();
})
</script>

<style scoped>
.hot-articles-card {
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
</style>