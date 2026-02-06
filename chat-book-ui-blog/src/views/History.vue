<template>
    <div class="history-page">
        <div class="history-container">
            <div class="page-header">
                <h2 class="title">浏览历史</h2>
                <p class="subtitle">记录您最近阅读过的精彩内容</p>
            </div>

            <div class="history-list-wrapper custom-scrollbar" @scroll="handleScroll">
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

                <div v-if="noMoreArticles && posts.length > 0" class="no-more">
                    <span>— 已经到底啦 —</span>
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
import ArticleCard from "@/components/widget/ArticleCard.vue";
import { onMounted, ref } from "vue";
import { getHistory } from "@/api/user.js";
import router from "@/router/index.js";

// 初始化为空数组，修复之前初始化为 [{}] 导致的空卡片问题
const posts = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(10); // 增加每页加载数量，提升体验

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

const handleScroll = (event) => {
    const { scrollTop, clientHeight, scrollHeight } = event.target;
    // 增加触发阈值，提前加载更流畅
    if (scrollTop + clientHeight >= scrollHeight - 100) {
        historyRequest();
    }
};

const openArticle = async (id) => {
    if (!id) return;
    await router.push({ name: 'Article', params: { id: id } });
};

onMounted(() => {
    historyRequest();
});
</script>

<style scoped>
.history-page {
    background-color: var(--bg-color-base);
    height: 100%;
    display: flex;
    justify-content: center;
    background: radial-gradient(circle at bottom right, #f0f4ff, var(--bg-color-base));
}

.history-container {
    width: 100%;
    max-width: 900px;
    padding: 32px 24px;
    display: flex;
    flex-direction: column;
    height: 100%;
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
    overflow-y: auto;
    padding-bottom: 40px;
    padding-right: 8px;
    /* For scrollbar space */
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 20px;
}

.history-item-wrapper {
    /* Animation base */
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
    /* Slight inner padding for framing effect */
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
    to {
        transform: rotate(360deg);
    }
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

/* Custom Scrollbar */
.custom-scrollbar::-webkit-scrollbar {
    width: 6px;
}

.custom-scrollbar::-webkit-scrollbar-track {
    background: transparent;
}

.custom-scrollbar::-webkit-scrollbar-thumb {
    background: rgba(0, 0, 0, 0.1);
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: rgba(0, 0, 0, 0.2);
}
</style>