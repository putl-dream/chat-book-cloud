<template>
    <div class="c-user-page c-user-page--centered c-user-history">
        <div class="c-user-page__container c-user-page__container--medium c-user-history__container">
            <div class="c-page-header c-page-header--accent c-user-history__header">
                <h2 class="c-page-header__title c-page-header__title--gradient">浏览历史</h2>
                <p class="c-page-header__subtitle">记录您最近阅读过的精彩内容</p>
            </div>

            <div class="c-user-history__list-wrap">
                <div class="c-user-history-list">
                    <transition-group name="staggered-fade">
                        <div v-for="(post, index) in posts" :key="post.id || index" class="c-user-history-list__item"
                            :style="{ '--delay': `${(index % 10) * 0.05}s` }" @click="openArticle(post.id)">
                            <div class="c-user-history-list__card">
                                <ArticleCard :post="post" />
                            </div>
                        </div>
                    </transition-group>
                </div>

                <div v-if="loading" class="c-loading-state c-user-history__loading">
                    <div class="c-spinner"></div>
                </div>

                <div ref="loadMoreTrigger" class="c-user-history-list__trigger"></div>

                <div v-if="noMoreArticles && posts.length > 0" class="c-status-note c-user-history__status">
                    <span>已经到底啦</span>
                </div>

                <div v-if="!loading && posts.length === 0 && noMoreArticles" class="c-empty-panel c-user-history__empty">
                    <el-empty description="暂无浏览历史" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
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
