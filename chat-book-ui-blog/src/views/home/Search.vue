<template>
    <div class="search-page">
        <!-- Background Decorations -->
        <div class="bg-decoration bg-decoration-1"></div>
        <div class="bg-decoration bg-decoration-2"></div>

        <div class="search-container">
            <div class="search-header-area">
                <div class="search-bar-wrapper">
                    <div class="search-input-group c-search-shell" @keyup.enter="handleSearch">
                        <input class="c-search-shell__input" type="text" v-model="keyValue" placeholder="搜索你感兴趣的文章.." />
                        <button class="c-search-shell__button" @click="handleSearch">
                            <el-icon class="search-icon">
                                <Search />
                            </el-icon>
                        </button>
                    </div>
                </div>
            </div>

            <div class="search-content">


                <div class="search-main-list c-glass-panel">
                    <div v-if="loading && currentPage === 1" class="loading c-loading-state c-glass-panel">
                        <el-skeleton :rows="5" animated />
                    </div>
                    <template v-else>
                        <transition-group name="list" tag="div">
                            <div v-for="(post, index) in posts" :key="post.id || index" class="post-item"
                                @click="openArticle(post.id)">
                                <ArticleCard :post="post" />
                            </div>
                        </transition-group>

                        <div v-if="loading && currentPage > 1" class="loading c-loading-state c-glass-panel">
                            <el-skeleton :rows="2" animated />
                        </div>
                        <div v-if="noMoreArticles && posts.length > 0" class="no-more c-status-note">没有更多文章啦</div>
                        <div v-if="posts.length === 0 && !loading" class="empty-state c-empty-panel">
                            <el-empty description="未找到相关文章" />
                        </div>
                    </template>
                </div>

                <div class="search-sidebar">
                    <div class="sidebar-card c-glass-panel">
                        <HotCard />
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import { ElSkeleton, ElIcon } from "element-plus";
import { Search } from "@element-plus/icons-vue";
import ArticleCard from "@/views/article/components/ArticleCard.vue";
import HotCard from "@/views/home/components/HotCard.vue";
import { useRoute } from "vue-router";
import { getLikePage } from "@/views/article/_domain/article.js";
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
        if (response) {
            const newPosts = response.list;
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
    await router.push({ name: 'Article', params: { id: id } })
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
    height: 100%;
    background-color: var(--bg-color-base);
    padding-top: 40px;
    position: relative;
    overflow: hidden;
}

/* Background Decorations */
.bg-decoration {
    position: absolute;
    border-radius: 50%;
    filter: blur(80px);
    z-index: 0;
    opacity: 0.6;
}

.bg-decoration-1 {
    top: -100px;
    left: -100px;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.2) 0%, rgba(37, 99, 235, 0) 70%);
}

.bg-decoration-2 {
    bottom: 10%;
    right: -5%;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.15) 0%, rgba(16, 185, 129, 0) 70%);
}

.search-container {
    max-width: var(--container-width-lg);
    margin: 0 auto;
    padding: 0 20px;
    position: relative;
    z-index: 1;
}

.search-header-area {
    margin-bottom: 40px;
    display: flex;
    justify-content: center;
    animation: fadeInDown 0.8s ease-out;
}

.search-bar-wrapper {
    width: 100%;
    max-width: 700px;
}

.search-input-group {
    --search-shell-height: 56px;
    --search-shell-radius: var(--border-radius-round);
    --search-shell-border: rgba(255, 255, 255, 0.5);
    --search-shell-bg: var(--bg-color-glass);
    --search-shell-blur: var(--blur-base);
    --search-shell-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
    --search-shell-focus-ring: 0 12px 40px rgba(37, 99, 235, 0.15);
    --search-shell-button-width: 80px;
    --search-shell-button-hover-bg: rgba(37, 99, 235, 0.1);
    --search-shell-button-hover-transform: scale(1.05);
}

.search-input-group:focus-within {
    transform: translateY(-2px);
}

.search-input-group .c-search-shell__input {
    padding: 0 32px;
    font-size: 1.1rem;
    font-weight: 500;
}

.search-input-group .c-search-shell__input::placeholder {
    font-weight: 400;
}

.search-icon {
    font-size: 24px;
    font-weight: bold;
}

.search-content {
    display: flex;
    gap: 20px;
    align-items: flex-start;
    animation: fadeInUp 0.8s ease-out 0.2s backwards;
}

.search-main-list,
.sidebar-card {
    --surface-bg: var(--bg-color-glass);
    --surface-blur: 20px;
    --surface-border: rgba(255, 255, 255, 0.6);
    --surface-shadow: var(--box-shadow-glass);
}

.search-main-list {
    flex: 1;
    min-height: 400px;
}

.post-item {
    margin-bottom: 20px;
    transition: all 0.3s ease;
}

.post-item:hover {
    transform: translateY(-4px);
}

.search-sidebar {
    width: 320px;
    flex-shrink: 0;
}

.loading {
    --feedback-loading-padding: 40px 0;
    --surface-padding: 24px;
    --surface-bg: rgba(255, 255, 255, 0.35);
    --surface-border: transparent;
    --surface-shadow: none;
    --surface-blur: 0px;
}

.no-more {
    --feedback-note-padding: 40px 0;
    --feedback-note-color: var(--text-color-secondary);
    --feedback-note-weight: 500;
}

.empty-state {
    --empty-panel-padding: 40px 0;
    --empty-panel-border: none;
    --empty-panel-bg: transparent;
    --empty-panel-blur: 0px;
}

/* List Transitions */
.list-enter-active,
.list-leave-active {
    transition: all 0.5s ease;
}

.list-enter-from,
.list-leave-to {
    opacity: 0;
    transform: translateY(20px);
}

/* Animations */
@keyframes fadeInDown {
    from {
        opacity: 0;
        transform: translateY(-20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* Responsive Design */
@media (max-width: 992px) {
    .search-content {
        flex-direction: column;
    }

    .search-sidebar {
        width: 100%;
    }

    .search-header-area {
        margin-bottom: 24px;
    }
}
</style>
