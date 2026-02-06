<template>
    <div class="history-page">
        <div class="history-container">
            <div class="page-header">
                <h2 class="title">浏览历史</h2>
                <p class="subtitle">记录您最近阅读过的精彩内容</p>
            </div>

            <div class="history-list-wrapper" @scroll="handleScroll">
                <div class="history-list">
                    <div v-for="(post, index) in posts" :key="post.id || index" class="history-item"
                        @click="openArticle(post.id)">
                        <ArticleCard :post="post" />
                    </div>
                </div>

                <div v-if="loading" class="loading-state">
                    <el-skeleton :rows="3" animated />
                </div>

                <div v-if="noMoreArticles && posts.length > 0" class="no-more">
                    <span>— 已经到底啦 —</span>
                </div>

                <div v-if="!loading && posts.length === 0 && noMoreArticles" class="empty-state">
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
const posts = ref([{
    id: 0,
    title: '暂无浏览历史',
    description: '暂无浏览历史',
    cover: '',
    createTime: '',
    readCount: 0,
    commentCount: 0,
    likeCount: 0,
    tags: []
}]);
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
    if (scrollTop + clientHeight >= scrollHeight - 50) {
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
}

.history-container {
    width: 100%;
    max-width: 800px;
    /* 限制最大宽度，保持阅读体验 */
    padding: 24px;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.page-header {
    margin-bottom: 24px;
    flex-shrink: 0;
}

.title {
    font-size: 1.5rem;
    font-weight: 600;
    color: var(--text-color-primary);
    margin: 0 0 8px 0;
}

.subtitle {
    font-size: 0.875rem;
    color: var(--text-color-secondary);
    margin: 0;
}

.history-list-wrapper {
    flex: 1;
    overflow-y: auto;
    /* 隐藏滚动条但保留功能 (Chrome/Safari/Webkit) */
    scrollbar-width: none;
    /* Firefox */
    -ms-overflow-style: none;
    /* IE 10+ */
    padding-bottom: 20px;
}

.history-list-wrapper::-webkit-scrollbar {
    display: none;
}

.history-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.history-item {
    transition: transform 0.2s ease;
    cursor: pointer;
}

/* 移除之前的丑陋样式，使用 ArticleCard 自带的样式 */
/* .history-item:hover {
    transform: translateY(-2px);
} */

.loading-state {
    padding: 24px 0;
}

.no-more {
    text-align: center;
    padding: 24px 0;
    color: var(--text-color-secondary);
    font-size: 0.875rem;
}

.empty-state {
    padding: 48px 0;
    display: flex;
    justify-content: center;
}
</style>