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


<style scoped>
.dashboard {
    padding: 24px;
    min-height: 100vh;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
}

.articles-container {
    max-width: 1000px;
    margin: 0 auto;
    height: calc(100vh - 48px);
    overflow-y: auto;
    padding-right: 8px; /* For scrollbar space */
}

/* Custom Scrollbar for Glassmorphism feel */
.articles-container::-webkit-scrollbar {
    width: 6px;
}

.articles-container::-webkit-scrollbar-track {
    background: transparent;
}

.articles-container::-webkit-scrollbar-thumb {
    background: rgba(156, 163, 175, 0.5);
    border-radius: 3px;
}

.articles-container::-webkit-scrollbar-thumb:hover {
    background: rgba(156, 163, 175, 0.8);
}

.article-card {
    margin-bottom: 24px;
    animation: fadeIn 0.5s ease-out;
}

.loading {
    --feedback-loading-padding: 20px;
    --surface-padding: 20px;
    --surface-bg: rgba(255, 255, 255, 0.5);
    --surface-radius: 12px;
    --surface-border: transparent;
    --surface-shadow: none;
    --surface-blur: 0px;
    margin-bottom: 20px;
}

.no-more {
    --feedback-note-padding: 32px 0;
    --feedback-note-size: 14px;
    --feedback-note-line-color: #e5e7eb;
    margin: 0;
}

@keyframes fadeIn {
    from {
        opacity: 0;
        transform: translateY(20px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}
</style>
