<template>
    <div class="article-detail">
        <div class="article-buttons">
            <div>
                <el-button class="comment-button" size="large" type="warning" :icon="Pointer" circle
                    :style="{ backgroundColor: praiseStat === 0 ? 'white' : 'orange', color: praiseStat === 0 ? 'orange' : 'white' }"
                    @click="handleLike" />
            </div>
            <div>
                <el-button class="comment-button" size="large" type="warning" :icon="ChatLineRound" circle
                    style="background-color: white; color: orange" @click="handleComment" />
            </div>
            <div>
                <el-button class="comment-button" size="large" type="primary" :icon="Service" circle
                    @click="handleAiChat" />
            </div>
            <div>
                <el-button class="comment-button" size="large" type="warning" :icon="Star" circle
                    :style="{ backgroundColor: collectStat === 0 ? 'white' : 'orange', color: collectStat === 0 ? 'orange' : 'white' }"
                    @click="handleFavorite" />
            </div>
        </div>

        <div class="content">
            <header class="article-header">
                <h1>{{ article.title }}</h1>
                <div class="article-meta">
                    <span class="gap">作者: {{ article.author }}</span>
                    <span class="gap">发布日期: {{ article.createTime }}</span>
                    <span class="gap">阅读量: {{ article.viewCount }}</span>
                </div>
            </header>
            <main class="article-content">
                <MarkdownRenderer :content="article.content" />
            </main>
        </div>

        <div class="article-right" :class="{ 'expanded-panel': activePanel !== 'default' }">
            <keep-alive>
                <component :is="componentMap[activePanel]" :userId="article.authorId" :articleId="articleId" />
            </keep-alive>
        </div>
    </div>
</template>
<script setup>
import { onMounted, ref } from 'vue';
import { ChatLineRound, Pointer, Star, Service } from '@element-plus/icons-vue';
import { useRoute } from "vue-router";
import { queryArticle } from "@/api/article.js";
import { updateCollection, updatePraise } from "@/api/user.js";
import { ElButton, ElMessage } from 'element-plus';
import { checkLogin } from "@/utils/index.js";
import MarkdownRenderer from "@/components/MarkdownRenderer.vue";

import SidebarDefault from '@/views/article-sidebar/SidebarDefault.vue';
import SidebarComment from '@/views/article-sidebar/SidebarComment.vue';
import SidebarAI from '@/views/article-sidebar/SidebarAI.vue';

// 获取路由参数
const route = useRoute();
const articleId = route.params.id;

const article = ref({});
const praiseStat = ref(0);
const collectStat = ref(0);
const activePanel = ref('default'); // default, comment, ai

const componentMap = {
    'default': SidebarDefault,
    'comment': SidebarComment,
    'ai': SidebarAI
};

const handleLike = async () => {
    if (!checkLogin()) return;
    const res = await updatePraise(articleId);
    console.log("点赞", res);
    praiseStat.value = res;
};

const handleComment = () => {
    if (activePanel.value === 'comment') {
        activePanel.value = 'default';
    } else {
        activePanel.value = 'comment';
    }
};

const handleAiChat = () => {
    if (activePanel.value === 'ai') {
        activePanel.value = 'default';
    } else {
        activePanel.value = 'ai';
    }
};

const handleFavorite = async () => {
    if (!checkLogin()) return;
    const res = await updateCollection(articleId);
    console.log('收藏', res);
    if (res === 0) {
        ElMessage.warning('取消收藏');
    } else {
        ElMessage.success('收藏成功');
    }
    collectStat.value = res;
};

const queryArticleRequest = async () => {
    const res = await queryArticle(articleId);
    if (res) {
        article.value = res;
        praiseStat.value = article.value.praiseStat;
        collectStat.value = article.value.collectStat;
        console.log("article->", article.value);
    }
};

onMounted(() => {
    queryArticleRequest();
});
</script>
<style scoped>
.article-detail {
    display: flex;
    max-width: var(--container-width-lg);
    margin: 0 auto;
    gap: 8px;
    padding: 8px var(--container-padding);
    position: relative;
    align-items: flex-start;
    height: calc(100vh - var(--header-height) - 40px);
    overflow: hidden;
    box-sizing: border-box;
}

.content {
    flex: 1;
    min-width: 0;
    max-width: 840px;
    padding: 40px;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    height: 100%;
    overflow-y: auto;
}

.article-header {
    border-bottom: 1px solid var(--border-color-light);
    padding-bottom: 16px;
    margin-bottom: 16px;
}

.article-header h1 {
    font-size: 2.25rem;
    font-weight: 800;
    color: var(--text-color-primary);
    line-height: 1.3;
    margin-bottom: 16px;
}

.article-meta {
    display: flex;
    gap: 16px;
    font-size: 0.875rem;
    color: var(--text-color-secondary);
}

.article-content {
    font-size: 1.0625rem;
    line-height: 1.8;
    color: var(--text-color-regular);
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 16px;
    height: fit-content;
    align-self: center;
    padding-right: 4px;
}

.comment-button {
    width: 48px;
    height: 48px;
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    transition: var(--transition-base);
}

.comment-button:hover {
    transform: scale(1.1);
    box-shadow: var(--box-shadow-hover);
}

.article-right {
    width: 320px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
    height: 100%;
    overflow-y: auto;
}

.article-right.expanded-panel {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    height: 100%;
    overflow: hidden;
}
</style>
