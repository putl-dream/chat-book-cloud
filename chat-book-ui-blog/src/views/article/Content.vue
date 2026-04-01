<template>
    <div class="dashboard">
        <!-- 文章列表卡片 -->
        <div class="articles-container" @scroll="handleScroll">
            <div v-if="loading" class="loading c-loading-state c-glass-panel">
                <el-skeleton :rows="5" animated />
            </div>
            <div v-for="(article, index) in articles" :key="index" class="article-card">
                <ArticleImgCard :post="article" />
            </div>
            <div v-if="noMoreArticles" class="no-more c-status-note c-status-note--lines">没有了</div>
            <div v-if="articles.length === 0" class="no-more c-status-note c-status-note--lines">这里空空如也</div>
        </div>
    </div>
</template>


<script setup>
import { onMounted } from 'vue';
import ArticleImgCard from '@/views/article/components/ArticleImgCard.vue';
import { useContentLogic } from './_hooks/useContentLogic.js';

const {
    articles,
    loading,
    noMoreArticles,
    fetchArticles,
    handleScroll
} = useContentLogic();

// 初始化时获取文章列表
onMounted(() => {
    fetchArticles();
});
</script>
