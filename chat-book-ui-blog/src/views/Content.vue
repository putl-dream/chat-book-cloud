<template>
    <div class="dashboard">
        <!-- 文章列表卡片 -->
        <div class="articles-container" @scroll="handleScroll">
            <div v-if="loading" class="loading">
                <el-skeleton :rows="5" animated/>
            </div>
            <div v-for="(article, index) in articles" :key="index" class="article-card" >
                <ArticleImgCard :post="article"/>
            </div>
            <div v-if="noMoreArticles " class="no-more">没有了</div>
            <div v-if="articles.length === 0" class="no-more">这里空空如也！</div>
        </div>
    </div>
</template>


<script setup>
import {ref, onMounted} from 'vue';
import ArticleImgCard from '@/components/widget/ArticleImgCard.vue';
import axios from 'axios';
import {getUserArticlePage} from "@/api/article.js";
import {getUserBySelf} from "@/api/user.js";
import router from "@/router/index.js"; // 假设你使用axios进行HTTP请求


// 文章列表数据
const articles = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const userId = ref(null);

// 获取文章列表
const fetchArticles = async () => {
    if (loading.value || noMoreArticles.value) return;
    loading.value = true;
    try {
        if (!userId.value) {
            const userRes = await getUserBySelf();
            if (userRes.code === 200 && userRes.data) {
                userId.value = userRes.data.id;
            }
        }
        if (!userId.value) {
             loading.value = false;
             return;
        }

        const response = await getUserArticlePage(currentPage.value, pageSize.value, userId.value)
        console.log(response)
        const newArticles = response.data.records;
        const total = response.data.total;
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
    const {scrollTop, clientHeight, scrollHeight} = event.target;
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
    padding: 10px 100px;
}

.stats-container {
    display: flex;
    justify-content: space-around;
    margin-bottom: 20px;
}

.stat-card {
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    width: 150px;
}

.stat-label {
    font-weight: bold;
    margin-bottom: 5px;
}

.stat-value {
    font-size: 1.2em;
}

.articles-container {
    margin-top: 20px;
}

.section-title {
    font-size: 1.5em;
    margin-bottom: 10px;
}

.article-card {
    border-bottom: 1px solid #ccc;
    padding: 0 10px;
    margin-bottom: 10px;
}


.article-image {
    margin-right: 20px;
}

.article-info {
    display: flex;
    margin-right: 20px;
}

.article-content {
    min-width: 500px;
}

.article-title {
    font-size: 1.2em;
    margin-bottom: 5px;
}

.article-summary {
    color: #666;
    margin-bottom: 10px;
}

.article-stats {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
}

.stat-item {
    display: flex;
    align-items: center;
}

.stat-item .stat-label {
    font-weight: bold;
    margin-right: 5px;
}

.stat-item .stat-value {
    font-size: 1em;
}

.no-more {
    margin-top: 20px;
    color: #999;
    text-align: center;
}
</style>
