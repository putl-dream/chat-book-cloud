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
                        <transition-group name="search-list" tag="div">
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
