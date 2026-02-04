<template>
    <div class="search-page">
        <div class="search-container">
            <div class="search-header-area">
                <div class="search-bar-wrapper">
                    <div class="search-input-group" @keyup.enter="handleSearch">
                        <input type="text" v-model="keyValue" placeholder="搜索文章..."/>
                        <button @click="handleSearch">搜索</button>
                    </div>
                </div>
            </div>

            <div class="search-content">
                <div class="search-main-list">
                    <div v-if="loading && currentPage === 1" class="loading">
                        <el-skeleton :rows="5" animated/>
                    </div>
                    <template v-else>
                        <div v-for="(post, index) in posts" :key="index" class="post-item" @click="openArticle(post.id)">
                            <ArticleCard :post="post"/>
                        </div>
                        <div v-if="loading && currentPage > 1" class="loading">
                            <el-skeleton :rows="2" animated/>
                        </div>
                        <div v-if="noMoreArticles && posts.length > 0" class="no-more">没有更多文章了</div>
                        <div v-if="posts.length === 0 && !loading" class="empty-state">
                            <el-empty description="未找到相关文章" />
                        </div>
                    </template>
                </div>
                
                <div class="search-sidebar">
                    <div class="sidebar-card">
                        <HotCard/>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import {ref, onMounted, onUnmounted} from 'vue';
import {ElSkeleton} from "element-plus";
import ArticleCard from "@/components/widget/ArticleCard.vue";
import HotCard from "@/components/widget/HotCard.vue";
import {useRoute} from "vue-router";
import {getLikePage} from "@/api/article.js";
import router from "@/router/index.js";

const posts = ref([]);
const keyValue = ref('');
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);

// 获取路由参数
const route = useRoute();

const fetchPosts = async (isLoadMore = false) => {
    if (loading.value || (noMoreArticles.value && isLoadMore)) return;
    
    loading.value = true;
    try {
        const response = await getLikePage(currentPage.value, pageSize.value, keyValue.value);
        if (response.code === 200) {
            const newPosts = response.data.records;
            if (newPosts.length === 0) {
                noMoreArticles.value = true;
            } else {
                if (isLoadMore) {
                    posts.value = [...posts.value, ...newPosts];
                } else {
                    posts.value = newPosts;
                }
                currentPage.value++;
            }
        }
    } catch (error) {
        console.error('Failed to fetch posts:', error);
    } finally {
        loading.value = false;
    }
};

const handleSearch = async () => {
    currentPage.value = 1;
    noMoreArticles.value = false;
    posts.value = [];
    await fetchPosts(false);
    // 更新路由参数，但不刷新页面
    router.replace({ name: 'List', params: { keyValue: keyValue.value } });
};

const handleScroll = () => {
    const scrollTop = window.scrollY;
    const clientHeight = window.innerHeight;
    const scrollHeight = document.documentElement.scrollHeight;

    if (scrollTop + clientHeight >= scrollHeight - 50) {
        fetchPosts(true);
    }
};

const openArticle = async (id) => {
    await router.push({name: 'Article', params: {id: id}})
};

onMounted(() => {
    if (route.params.keyValue) {
        keyValue.value = route.params.keyValue;
        fetchPosts(false);
    }
    window.addEventListener('scroll', handleScroll);
});

onUnmounted(() => {
    window.removeEventListener('scroll', handleScroll);
});
</script>

<style scoped>
.search-page {
    min-height: 100vh;
    background-color: var(--bg-color-base);
    padding-top: 20px;
}

.search-container {
    max-width: var(--container-width-lg);
    margin: 0 auto;
    padding: 0 20px;
}

.search-header-area {
    margin-bottom: 24px;
    display: flex;
    justify-content: center;
}

.search-bar-wrapper {
    width: 100%;
    max-width: 600px;
}

.search-input-group {
    display: flex;
    border: 1px solid var(--border-color-base);
    border-radius: var(--border-radius-round);
    overflow: hidden;
    height: 44px;
    background: var(--bg-color-white);
    transition: all 0.3s;
    box-shadow: var(--box-shadow-base);
}

.search-input-group:focus-within {
    border-color: var(--color-primary);
    box-shadow: 0 0 0 3px var(--color-primary-light);
}

.search-input-group input {
    border: none;
    padding: 0 20px;
    flex: 1;
    outline: none;
    font-size: 16px;
    color: var(--text-color-primary);
}

.search-input-group button {
    width: 100px;
    background-color: var(--color-primary);
    color: white;
    border: none;
    cursor: pointer;
    font-size: 16px;
    font-weight: 500;
    transition: background-color 0.3s;
}

.search-input-group button:hover {
    background-color: var(--color-primary-hover);
}

.search-content {
    display: flex;
    gap: 24px;
    align-items: flex-start;
}

.search-main-list {
    flex: 1;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 20px;
    box-shadow: var(--box-shadow-base);
    min-height: 400px;
}

.post-item {
    border-bottom: 1px solid var(--border-color-lighter);
    padding: 16px 0;
    cursor: pointer;
    transition: background-color 0.2s;
}

.post-item:hover {
    background-color: var(--bg-color-base);
}

.post-item:last-child {
    border-bottom: none;
}

.search-sidebar {
    width: 300px;
    flex-shrink: 0;
}

.sidebar-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 20px;
    box-shadow: var(--box-shadow-base);
}

.loading, .no-more, .empty-state {
    text-align: center;
    padding: 20px;
    color: var(--text-color-secondary);
}

/* Responsive Design */
@media (max-width: 992px) {
    .search-content {
        flex-direction: column;
    }
    
    .search-sidebar {
        width: 100%;
    }
}
</style>
