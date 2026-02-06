<template>
    <div class="dashboard-page">
        <div class="bg-decoration"></div>

        <div class="dashboard-content animate-fade-in">
            <div class="page-header">
                <div class="header-content">
                    <h2 class="title">创作中心</h2>
                    <p class="subtitle">管理您的创作内容，查看数据表现</p>
                </div>
            </div>

            <!-- 数据展示卡片 (保留注释) -->
            <!-- <div class="data-cards">
                <UserDataCard/>
            </div> -->

            <!-- 文章列表卡片 -->
            <div class="articles-container">
                <div class="section-header">
                    <h3 class="section-title">近期文章</h3>
                    <div class="header-line"></div>
                </div>
                
                <div class="article-list">
                    <div v-for="(article, index) in articles" :key="index" class="article-card" :style="{ '--delay': index }">
                        <div class="article-content">
                            <div class="article-main">
                                <h3 class="article-title">{{ article.title }}</h3>
                                <p class="article-summary">{{ article.summary }}</p>
                            </div>
                            
                            <div class="article-stats">
                                <div class="stat-item" title="阅读量">
                                    <el-icon><View /></el-icon>
                                    <span class="stat-value">{{ article.viewCount }}</span>
                                </div>
                                <div class="stat-item" title="评论量">
                                    <el-icon><ChatLineRound /></el-icon>
                                    <span class="stat-value">{{ article.commentCount }}</span>
                                </div>
                                <div class="stat-item" title="点赞数">
                                    <el-icon><Star /></el-icon>
                                    <span class="stat-value">{{ article.praiseCount }}</span>
                                </div>
                                <div class="stat-item" title="收藏数">
                                    <el-icon><Collection /></el-icon>
                                    <span class="stat-value">{{ article.collectCount }}</span>
                                </div>
                            </div>
                        </div>
                        
                        <!-- 悬停操作栏 (可选) -->
                        <div class="card-decoration"></div>
                    </div>
                </div>

                <div class="pagination-wrapper">
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
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import UserDataCard from "@/components/widget/UserDataCard.vue";
import { getUserArticlePage } from "@/api/article.js";
import { getUserBySelf } from "@/api/user.js";
import { View, ChatLineRound, Star, Collection } from '@element-plus/icons-vue';

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
            if (userRes) {
                userId.value = userRes.id;
            }
        }
        
        if (!userId.value) return;

        const response = await getUserArticlePage(currentPage.value, pageSize.value, userId.value)
        if (response === null) {return;}
        articles.value = response.records;
        console.log(articles.value)
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
.dashboard-page {
    min-height: 100vh;
    background-color: var(--bg-color-base);
    padding: 40px 20px;
    position: relative;
    overflow: hidden;
}

/* Background Decoration */
.bg-decoration {
    position: absolute;
    top: -100px;
    left: -100px;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, var(--color-primary-light) 0%, rgba(255,255,255,0) 70%);
    opacity: 0.5;
    z-index: 0;
    pointer-events: none;
}

.dashboard-content {
    position: relative;
    z-index: 1;
    max-width: var(--container-width-lg);
    margin: 0 auto;
}

/* Header */
.page-header {
    margin-bottom: 40px;
}

.title {
    font-size: 32px;
    font-weight: 800;
    color: var(--text-color-primary);
    margin: 0 0 8px 0;
    letter-spacing: -1px;
}

.subtitle {
    font-size: 16px;
    color: var(--text-color-secondary);
    margin: 0;
}

/* Articles Section */
.articles-container {
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-large);
    border: 1px solid rgba(255, 255, 255, 0.6);
    border-radius: var(--border-radius-xl);
    padding: 32px;
    box-shadow: var(--box-shadow-glass);
}

.section-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
}

.section-title {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-color-primary);
    margin: 0;
}

.header-line {
    flex: 1;
    height: 1px;
    background: linear-gradient(to right, var(--border-color-base), transparent);
}

/* Article Cards */
.article-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.article-card {
    background: var(--bg-color-white);
    border: 1px solid var(--border-color-light);
    border-radius: var(--border-radius-large);
    padding: 24px;
    transition: var(--transition-base);
    position: relative;
    overflow: hidden;
    
    /* Animation Stagger */
    animation: slideUpFade 0.5s cubic-bezier(0.2, 0.8, 0.2, 1) forwards;
    opacity: 0;
    animation-delay: calc(var(--delay) * 0.1s);
}

.article-card:hover {
    transform: translateY(-2px);
    box-shadow: var(--box-shadow-hover);
    border-color: var(--color-primary-light);
}

.article-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 24px;
}

.article-main {
    flex: 1;
    min-width: 0; /* Prevent text overflow issues */
}

.article-title {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin: 0 0 8px 0;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    transition: color 0.2s;
}

.article-card:hover .article-title {
    color: var(--color-primary);
}

.article-summary {
    font-size: 14px;
    color: var(--text-color-secondary);
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    line-height: 1.5;
}

.article-stats {
    display: flex;
    gap: 24px;
    flex-shrink: 0;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
    color: var(--text-color-secondary);
    min-width: 48px;
    transition: var(--transition-fast);
}

.stat-item .el-icon {
    font-size: 20px;
    margin-bottom: 4px;
}

.stat-value {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.article-card:hover .stat-item {
    color: var(--color-primary);
}

.card-decoration {
    position: absolute;
    left: 0;
    top: 0;
    bottom: 0;
    width: 4px;
    background: var(--color-primary);
    opacity: 0;
    transition: var(--transition-fast);
}

.article-card:hover .card-decoration {
    opacity: 1;
}

/* Pagination */
.pagination-wrapper {
    margin-top: 32px;
    display: flex;
    justify-content: center;
}

/* Animations */
.animate-fade-in {
    animation: fadeIn 0.8s ease-out forwards;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

@keyframes slideUpFade {
    from { opacity: 0; transform: translateY(20px); }
    to { opacity: 1; transform: translateY(0); }
}

/* Responsive */
@media (max-width: 768px) {
    .dashboard-page {
        padding: 20px;
    }
    
    .articles-container {
        padding: 20px;
    }
    
    .article-content {
        flex-direction: column;
        align-items: flex-start;
    }
    
    .article-stats {
        width: 100%;
        justify-content: space-between;
        margin-top: 16px;
        padding-top: 16px;
        border-top: 1px solid var(--border-color-lighter);
    }
    
    .stat-item {
        flex-direction: row;
        gap: 8px;
    }
    
    .stat-item .el-icon {
        margin-bottom: 0;
    }
}
</style>
