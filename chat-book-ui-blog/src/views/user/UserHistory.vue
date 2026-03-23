<template>
    <div class="history-page">
        <div class="history-container">
            <div class="page-header">
                <h2 class="title">浏览历史</h2>
                <p class="subtitle">记录您最近阅读过的精彩内容</p>
            </div>

            <div class="history-list-wrapper">
                <div class="history-list">
                    <transition-group name="staggered-fade">
                        <div v-for="(post, index) in posts" :key="post.id || index" class="history-item-wrapper"
                            :style="{ '--delay': `${(index % 10) * 0.05}s` }" @click="openArticle(post.id)">
                            <div class="history-card glass-panel">
                                <ArticleCard :post="post" />
                            </div>
                        </div>
                    </transition-group>
                </div>

                <div v-if="loading" class="loading-state">
                    <div class="loading-spinner">
                        <div class="spinner-ring"></div>
                    </div>
                </div>

                <!-- Intersection Observer trigger -->
                <div ref="loadMoreTrigger" class="load-more-trigger"></div>

                <div v-if="noMoreArticles && posts.length > 0" class="no-more">
                    <span>已经到底啦</span>
                </div>

                <div v-if="!loading && posts.length === 0 && noMoreArticles" class="empty-state glass-panel">
                    <el-empty description="暂无浏览历史" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ElSkeleton, ElEmpty } from "element-plus";
import ArticleCard from "@/views/article/components/ArticleCard.vue";
import { onMounted, onUnmounted, ref } from "vue";
import { getHistory } from "@/views/article/_domain/interaction.js";
import router from "@/router/index.js";

const posts = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const loadMoreTrigger = ref(null);
let observer = null;

const historyRequest = async () => {
    if (loading.value || noMoreArticles.value) return;
    loading.value = true;
    try {
        const res = await getHistory(currentPage.value, pageSize.value);
        if (!res || res.length === 0) {
            noMoreArticles.value = true;
            return;
        }
        posts.value = [...posts.value, ...res];
        currentPage.value++;
    } catch (error) {
        console.error('获取历史记录失败', error);
    } finally {
        loading.value = false;
    }
};

const openArticle = async (id) => {
    if (!id) return;
    await router.push({ name: 'Article', params: { id: id } });
};

onMounted(() => {
    historyRequest();
    
    // Setup Intersection Observer for seamless scroll functionality
    observer = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting && !loading.value && !noMoreArticles.value) {
            historyRequest();
        }
    }, {
        rootMargin: '100px',
        threshold: 0.1
    });

    if (loadMoreTrigger.value) {
        observer.observe(loadMoreTrigger.value);
    }
});

onUnmounted(() => {
    if (observer && loadMoreTrigger.value) {
        observer.unobserve(loadMoreTrigger.value);
    }
    if (observer) {
        observer.disconnect();
    }
});
</script>

<style scoped>
.history-page {
    /* Native scrolling via AppLayout */
    display: flex;
    justify-content: center;
    width: 100%;
    min-height: 100%;
}

.history-container {
    width: 100%;
    max-width: 900px;
    padding: 32px 24px;
    display: flex;
    flex-direction: column;
    margin: 0 auto;
}

.page-header {
    margin-bottom: 32px;
    flex-shrink: 0;
    position: relative;
    padding-left: 16px;
    border-left: 4px solid var(--color-primary);
}

.title {
    font-size: 2rem;
    font-weight: 800;
    color: var(--text-color-primary);
    margin: 0 0 4px 0;
    letter-spacing: -1px;
    background: linear-gradient(90deg, var(--text-color-primary), var(--color-primary));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.subtitle {
    font-size: 1rem;
    color: var(--text-color-secondary);
    margin: 0;
    font-weight: 500;
}

.history-list-wrapper {
    flex: 1;
    padding-bottom: 40px;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.history-item-wrapper {
    animation: fadeInUp 0.6s cubic-bezier(0.2, 0.8, 0.2, 1) backwards;
    animation-delay: var(--delay);
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(30px);
    }
    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.history-card {
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    transition: all 0.4s cubic-bezier(0.25, 0.8, 0.25, 1);
    cursor: pointer;
    overflow: hidden;
    padding: 4px;
}

.history-card:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: 0 16px 32px rgba(0, 0, 0, 0.08);
    background: rgba(255, 255, 255, 0.9);
    border-color: rgba(255, 255, 255, 0.9);
}

.loading-state {
    padding: 32px 0;
    display: flex;
    justify-content: center;
}

.spinner-ring {
    width: 32px;
    height: 32px;
    border: 3px solid rgba(0, 0, 0, 0.1);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

@keyframes spin {
    to { transform: rotate(360deg); }
}

.no-more {
    text-align: center;
    padding: 32px 0;
    color: var(--text-color-placeholder);
    font-size: 0.9rem;
    font-weight: 500;
    letter-spacing: 1px;
    text-transform: uppercase;
}

.empty-state {
    padding: 60px 0;
    display: flex;
    justify-content: center;
    background: rgba(255, 255, 255, 0.4);
    border-radius: 24px;
    backdrop-filter: blur(8px);
}

.load-more-trigger {
    width: 100%;
    height: 1px;
    opacity: 0;
}
</style>
