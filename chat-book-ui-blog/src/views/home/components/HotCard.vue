<template>
    <div class="hot-articles-card">
        <h3 class="card-title c-panel-title c-panel-title--compact">热门文章</h3>
        <ul class="article-list c-article-rank-list">
            <li v-for="(article, index) in hotArticles" :key="index" class="article-item c-article-rank-list__item" @click="openArticle(article.id)">
                <span class="rank-badge c-article-rank-list__badge" :class="getRankClass(index)">{{ index + 1 }}</span>
                <span class="article-title c-article-rank-list__title" :title="article.title">{{ article.title }}</span>
            </li>
        </ul>
    </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import {getHotPage} from "@/views/article/_domain/article.js";
import router from "@/router/index.js";

const hotArticles = ref([]);

const getRankClass = (index) => {
    if (index === 0) return 'c-article-rank-list__badge--top-1';
    if (index === 1) return 'c-article-rank-list__badge--top-2';
    if (index === 2) return 'c-article-rank-list__badge--top-3';
    return 'c-article-rank-list__badge--other';
};

const queryHotRequest = async () => {
    try {
        const response = await getHotPage(1, 10);
        if (response && response.list) {
            hotArticles.value = response.list;
        } else if (response && response.records) {
             // Fallback if the field name is records
             hotArticles.value = response.records;
        }
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
</style>
