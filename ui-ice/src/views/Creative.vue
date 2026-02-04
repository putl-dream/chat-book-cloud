<template>
    <div class="dashboard">
        <!-- 数据展示卡片 -->
<!--        <div class="data-cards">
            <UserDataCard/>
        </div>-->

        <!-- 文章列表卡片 -->
        <div class="articles-container">
            <h2 class="section-title">近期文章</h2>
            <div v-for="(article, index) in articles" :key="index" class="article-card">
                <div class="article-info">
                    <h3 class="article-title">{{ article.title }}</h3>
                    <el-text style="width: 500px" truncated>{{ article.summary }}</el-text>
                </div>
                <div class="article-stats">
                    <div class="stat-item">
                        <el-text class="stat-label">阅读量</el-text>
                        <p class="stat-value">{{ article.viewCount }}</p>
                    </div>
                    <div class="stat-item">
                        <el-text class="stat-label">评论量</el-text>
                        <p class="stat-value">{{ article.commentCount }}</p>
                    </div>
                    <div class="stat-item">
                        <el-text class="stat-label">点赞数</el-text>
                        <p class="stat-value">{{ article.praiseCount }}</p>
                    </div>
                    <div class="stat-item">
                        <el-text class="stat-label">收藏数</el-text>
                        <p class="stat-value">{{ article.collectCount }}</p>
                    </div>
                </div>
            </div>
            <el-pagination
                background
                layout="prev, pager, next"
                :total="totalArticles"
                :page-size="pageSize"
                v-model:current-page="currentPage"
                @current-change="handlePageChange"
            />
        </div>
    </div>
</template>

<script setup>
import {ref, onMounted} from 'vue';
import axios from 'axios';
import UserDataCard from "@/components/widget/UserDataCard.vue";
import {getUserArticlePage} from "@/api/article.js";
import {getUserBySelf} from "@/api/user.js";



// 文章列表数据
const articles = ref([]);
const totalArticles = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);
const userId = ref(null);

// 获取文章列表
const fetchArticles = async () => {
    try {
        if (!userId.value) {
            const userRes = await getUserBySelf();
            if (userRes.code === 200 && userRes.data) {
                userId.value = userRes.data.id;
            }
        }
        
        if (!userId.value) return;

        const response = await getUserArticlePage(currentPage.value, pageSize.value, userId.value)
        if (response.data === null) {return;}
        articles.value = response.data.records;
        console.log(articles.value)
        totalArticles.value = parseInt(response.data.total);
    } catch (error) {
        console.error('Failed to fetch articles:', error);
    }
};

// 分页变化处理
const handlePageChange = (newPage) => {
    currentPage.value = newPage;
    fetchArticles();
};

// 初始化时获取文章列表
onMounted(() => {
    fetchArticles();
});
</script>

<style scoped>
.dashboard {
    padding: 10px  100px;
}

.data-cards {
    background-color: #ffffff;
    padding: 20px 0;
}


.articles-container {
    margin-top: 20px;
}

.section-title {
    font-size: 1.5em;
    color: #333;
    padding: 30px 20px 30px 20px;
    background-color: #f9f9f9;
}

.article-card {
    display: flex;
    justify-content: space-between;
    padding: 20px;
/*    border-radius: 8px;*/
    margin-bottom: 10px;
    background-color: #f9f9f9;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.article-info {
    margin-right: 20px;
}

.article-title {
    font-size: 1.2em;
    margin-bottom: 5px;
}

.article-summary {
    color: #666;
}

.article-stats {
    min-width: 500px;
    display: flex;
    justify-content: space-around;
}

.stat-item {
    text-align: right;
}

.stat-item .stat-label {
    font-weight: bold;
    margin-bottom: 5px;
}

.stat-item .stat-value {
    font-size: 1.2em;
    color: #6120dc;
    font-weight: bold;
    margin-top: 5px;
    display: flex;
    justify-content: center;
}

.el-pagination {
    margin-top: 20px;
    justify-content: center;
}
</style>
