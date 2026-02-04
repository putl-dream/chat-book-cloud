<template>
    <div class="history-main-list" @scroll="handleScroll">
        <div>
            <div v-for="(post, index) in posts" :key="index" class="post" @click="openArticle(post.id)">
                <ArticleCard :post="post" />
            </div>
            <div v-if="loading" class="loading">
                <el-skeleton :rows="5" animated />
            </div>
            <div v-if="noMoreArticles" class="no-more">没有更多文章了</div>
        </div>
    </div>
</template>

<script setup>
import { ElSkeleton } from "element-plus";
import ArticleCard from "@/components/widget/ArticleCard.vue";
import { onMounted, ref } from "vue";
import { getHistory } from "@/api/user.js";
import router from "@/router/index.js";

const posts = ref([]);
const loading = ref(false);
const noMoreArticles = ref(false);
const currentPage = ref(1);
const pageSize = ref(6);

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
    if (scrollTop + clientHeight >= scrollHeight - 10) { // 距离底部10px时加载更多
        historyRequest(currentPage.value + 1);
    }
};
const openArticle = async (id) => {
    await router.push({ name: 'Article', params: { id: id } });
};
onMounted(() => {
    historyRequest();
});
</script>

<style scoped>
.history-main-list {
    margin-top: 0;
    height: calc(100vh - 60px);
    /* 根据需要调整高度 */
    overflow-y: auto;
    padding: 0 300px;
}

.post {
    border-bottom: 1px solid #ccc;
    padding: 20px;
    background-color: #e9e3fa;
}
</style>
