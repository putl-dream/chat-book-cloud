<template>
    <div class="dashboard">
        <!-- 文章列表卡片 -->
        <div class="articles-container" @scroll="handleScroll">
            <div v-if="loading" class="loading">
                <el-skeleton :rows="5" animated />
            </div>
            <div v-for="(article, index) in articles" :key="index" class="article-card">
                <ArticleImgCard :post="article" />
            </div>
            <div v-if="noMoreArticles" class="no-more">没有了</div>
            <div v-if="articles.length === 0" class="no-more">这里空空如也！</div>
        </div>
    </div>
</template>


<script setup>
import { onMounted, ref } from 'vue';
import ArticleImgCard from '@/components/widget/ArticleImgCard.vue';
import { getUserArticlePage } from "@/api/article.js";


// 文章列表数据
const articles = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);

// 获取文章列表
const fetchArticles = async () => {
    if (loading.value || noMoreArticles.value) return;
    loading.value = true;
    try {
        const response = await getUserArticlePage(currentPage.value, pageSize.value)
        console.log(response)
        const newArticles = response.list;
        const total = response.total;
        if (newArticles.length === 0) {
            noMoreArticles.value = true;
        } else {
            articles.value = currentPage.value === 1 ? newArticles : [...articles.value, ...newArticles];
            currentPage.value++;
        }
    } catch (error) {
        console.error('Failed to fetch articles:', error);
    } finally {
        loading.value = false;
    }
};

// 滚动事件处理
const handleScroll = (event) => {
    const { scrollTop, clientHeight, scrollHeight } = event.target;
    if (scrollTop + clientHeight >= scrollHeight - 10) { // 调整阈值以适应需要
        fetchArticles();
    }
};

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
    padding: 20px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 12px;
    margin-bottom: 20px;
}

.no-more {
    margin: 32px 0;
    color: #9ca3af;
    text-align: center;
    font-size: 14px;
    position: relative;
}

.no-more::before,
.no-more::after {
    content: '';
    position: absolute;
    top: 50%;
    width: 50px;
    height: 1px;
    background: #e5e7eb;
}

.no-more::before {
    margin-right: 15px;
    right: 50%;
    transform: translateX(-20px);
}

.no-more::after {
    margin-left: 15px;
    left: 50%;
    transform: translateX(20px);
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
