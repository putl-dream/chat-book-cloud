<template>
    <div class="dashboard animate-fade-in">
        <!-- 数据展示卡片 (Reserved) -->
        <!-- <div class="data-cards">
            <UserDataCard/>
        </div> -->

        <!-- 文章列表卡片 -->
        <div class="articles-container">
            <div class="section-header">
                <h2 class="section-title">近期文章</h2>
                <div class="section-decoration"></div>
            </div>

            <div class="article-list">
                <div v-for="(article, index) in articles" :key="index" class="article-card"
                    :style="{ animationDelay: `${index * 0.1}s` }">
                    <div class="article-main">
                        <h3 class="article-title">{{ article.title }}</h3>
                        <p class="article-summary">{{ article.summary }}</p>
                        <div class="article-meta">
                            <span class="date">{{ formatDate(article.createTime) }}</span>
                        </div>
                    </div>

                    <div class="article-stats">
                        <div class="stat-item" title="阅读量">
                            <el-icon class="stat-icon">
                                <View />
                            </el-icon>
                            <span class="stat-value">{{ formatNumber(article.viewCount) }}</span>
                            <span class="stat-label">阅读</span>
                        </div>
                        <div class="stat-item" title="评论量">
                            <el-icon class="stat-icon">
                                <ChatDotSquare />
                            </el-icon>
                            <span class="stat-value">{{ formatNumber(article.commentCount) }}</span>
                            <span class="stat-label">评论</span>
                        </div>
                        <div class="stat-item" title="点赞数">
                            <el-icon class="stat-icon">
                                <Star />
                            </el-icon>
                            <span class="stat-value">{{ formatNumber(article.praiseCount) }}</span>
                            <span class="stat-label">点赞</span>
                        </div>
                        <div class="stat-item" title="收藏数">
                            <el-icon class="stat-icon">
                                <Collection />
                            </el-icon>
                            <span class="stat-value">{{ formatNumber(article.collectCount) }}</span>
                            <span class="stat-label">收藏</span>
                        </div>
                    </div>

                    <div class="card-actions">
                        <el-button link type="primary">编辑</el-button>
                        <el-button link type="danger">删除</el-button>
                    </div>
                </div>
            </div>

            <div class="pagination-wrapper" v-if="totalArticles > 0">
                <el-pagination background layout="prev, pager, next" :total="totalArticles" :page-size="pageSize"
                    v-model:current-page="currentPage" @current-change="handlePageChange" />
            </div>

            <el-empty v-else description="暂无文章，快去创作吧！" class="empty-state"></el-empty>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import UserDataCard from "@/components/widget/UserDataCard.vue";
import { getUserArticlePage } from "@/api/article.js";
import { View, ChatDotSquare, Star, Collection } from '@element-plus/icons-vue';

// 文章列表数据
const articles = ref([]);
const totalArticles = ref(0);
const pageSize = ref(10);
const currentPage = ref(1);

// Format Helper
const formatNumber = (num) => {
    if (!num) return 0;
    return num > 9999 ? (num / 10000).toFixed(1) + 'w' : num;
};

const formatDate = (dateStr) => {
    if (!dateStr) return '';
    return dateStr.split('T')[0]; // Simple format
};

// 获取文章列表
const fetchArticles = async () => {
    try {
        const response = await getUserArticlePage(currentPage.value, pageSize.value)
        if (response === null) { return; }
        articles.value = response.list;
        totalArticles.value = parseInt(response.total);
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
    padding: 24px;
    min-height: 100vh;
    background: linear-gradient(135deg, #f3f4f6 0%, #e5e7eb 100%);
}

.articles-container {
    max-width: 1000px;
    margin: 0 auto;
    position: relative;
    height: calc(100vh - 48px);
    overflow-y: auto;
    padding-right: 8px;
    /* For scrollbar space */
}

/* Custom Scrollbar */
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

.animate-fade-in {
    animation: fadeIn 0.6s ease-out;
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

.section-header {
    display: flex;
    align-items: center;
    margin-bottom: 24px;
    position: relative;
    padding-top: 8px;
}

.section-title {
    font-size: 24px;
    font-weight: 700;
    color: #1f2937;
    margin: 0;
    z-index: 1;
}

.section-decoration {
    position: absolute;
    bottom: -4px;
    left: 0;
    width: 60px;
    height: 8px;
    background: rgba(59, 130, 246, 0.2);
    border-radius: 4px;
    z-index: 0;
}

.article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.article-card {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 24px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    animation: slideUp 0.5s ease-out forwards;
    opacity: 0;
    margin-bottom: 0;
    /* Handled by flex gap */
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.article-card:hover {
    transform: translateY(-2px);
    background: rgba(255, 255, 255, 0.9);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
}

.article-main {
    flex: 1;
    margin-right: 32px;
    min-width: 0;
    /* fix flex truncation */
}

.article-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0 0 8px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.2s;
    cursor: pointer;
}

.article-title:hover {
    color: #3b82f6;
}

.article-summary {
    color: #6b7280;
    font-size: 14px;
    line-height: 1.6;
    margin: 0 0 12px 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.article-meta {
    font-size: 12px;
    color: #9ca3af;
    display: flex;
    align-items: center;
}

.date {
    background: rgba(0, 0, 0, 0.05);
    padding: 2px 8px;
    border-radius: 4px;
}

.article-stats {
    display: flex;
    gap: 24px;
    padding: 0 24px;
    border-left: 1px solid rgba(229, 231, 235, 0.5);
    border-right: 1px solid rgba(229, 231, 235, 0.5);
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    min-width: 48px;
    cursor: default;
}

.stat-icon {
    font-size: 20px;
    color: #9ca3af;
    margin-bottom: 4px;
    transition: all 0.3s;
}

.stat-value {
    font-size: 16px;
    font-weight: 700;
    color: #4b5563;
    line-height: 1.2;
}

.stat-label {
    font-size: 12px;
    color: #9ca3af;
    margin-top: 2px;
}

.article-card:hover .stat-item:hover .stat-icon {
    color: #3b82f6;
    transform: scale(1.1);
}

.card-actions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    padding-left: 24px;
}

.card-actions .el-button {
    margin: 0;
}

.pagination-wrapper {
    margin-top: 32px;
    display: flex;
    justify-content: center;
    margin-bottom: 32px;
}

.empty-state {
    padding: 60px 0;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    backdrop-filter: blur(10px);
}

/* Responsive */
@media (max-width: 1024px) {
    .article-card {
        flex-direction: column;
        align-items: flex-start;
        gap: 20px;
    }

    .article-main {
        margin-right: 0;
        width: 100%;
    }

    .article-stats {
        width: 100%;
        border: none;
        border-top: 1px solid #e5e7eb;
        padding: 16px 0 0 0;
        justify-content: space-between;
    }

    .card-actions {
        flex-direction: row;
        width: 100%;
        justify-content: flex-end;
        padding-left: 0;
    }
}
</style>
