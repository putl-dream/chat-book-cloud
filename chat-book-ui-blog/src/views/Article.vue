<template>
    <div class="article-page">
        <div class="article-detail">
            <div class="article-buttons glass-panel-mini">
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': praiseStat !== 0 }"
                        @click="handleLike">
                        <el-icon>
                            <Pointer />
                        </el-icon>
                    </el-button>
                    <span class="action-label">点赞</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle
                        :class="{ 'is-active': activePanel === 'comment' }" @click="handleComment">
                        <el-icon>
                            <ChatLineRound />
                        </el-icon>
                    </el-button>
                    <span class="action-label">评论</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn ai-btn" size="large" circle
                        :class="{ 'is-active': activePanel === 'ai' }" @click="handleAiChat">
                        <el-icon>
                            <Service />
                        </el-icon>
                    </el-button>
                    <span class="action-label">AI助手</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }"
                        @click="handleFavorite">
                        <el-icon>
                            <Star />
                        </el-icon>
                    </el-button>
                    <span class="action-label">收藏</span>
                </div>
            </div>

            <div class="content glass-panel">
                <header class="article-header">
                    <h1 class="article-title">{{ article.title }}</h1>
                    <div class="article-meta">
                        <div class="meta-item">
                            <span class="label">作者</span>
                            <span class="value">{{ article.author }}</span>
                        </div>
                        <div class="meta-divider"></div>
                        <div class="meta-item">
                            <span class="label">发布于</span>
                            <span class="value">{{ article.createTime }}</span>
                        </div>
                        <div class="meta-divider"></div>
                        <div class="meta-item">
                            <span class="label">阅读</span>
                            <span class="value">{{ article.viewCount }}</span>
                        </div>
                    </div>
                </header>
                <main class="article-content custom-scrollbar">
                    <MarkdownRenderer :content="article.content" />
                </main>
            </div>

            <div class="article-right" :class="{ 'expanded-panel glass-panel': activePanel !== 'default' }">
                <transition name="fade-slide" mode="out-in">
                    <keep-alive>
                        <component :is="componentMap[activePanel]" :userId="article.authorId" :articleId="articleId" />
                    </keep-alive>
                </transition>
            </div>
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
import { checkLogin } from "@/utils/http.js";
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
    }
};

onMounted(() => {
    queryArticleRequest();
});
</script>

<style scoped>
.article-page {
    height: 100%;
    background-color: var(--bg-color-base);
    background: radial-gradient(circle at top left, #f8fafc, var(--bg-color-base));
}

.article-detail {
    display: flex;
    max-width: 1400px;
    margin: 0 auto;
    gap: 12px;
    padding: 24px var(--container-padding);
    position: relative;
    align-items: flex-start;
    height: 100%;
    overflow: hidden;
    box-sizing: border-box;
}

.glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
    border-radius: 24px;
}

.glass-panel-mini {
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
    border-radius: 50px;
}

.content {
    flex: 1;
    min-width: 0;
    padding: 40px 60px;
    height: 100%;
    display: flex;
    flex-direction: column;
    margin-left: 96px;
}

.article-header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    padding-bottom: 24px;
    margin-bottom: 24px;
}

.article-title {
    font-size: 2.5rem;
    font-weight: 800;
    color: var(--text-color-primary);
    line-height: 1.3;
    margin: 0 0 24px;
    letter-spacing: -0.02em;
}

.article-meta {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 0.875rem;
    color: var(--text-color-secondary);
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 6px;
}

.meta-item .label {
    opacity: 0.7;
}

.meta-item .value {
    font-weight: 500;
    color: var(--text-color-primary);
}

.meta-divider {
    width: 1px;
    height: 12px;
    background: rgba(0, 0, 0, 0.1);
}

.article-content {
    flex: 1;
    overflow-y: auto;
    font-size: 1.1rem;
    line-height: 1.8;
    color: var(--text-color-regular);
    padding-right: 10px;
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 24px;
    height: fit-content;
    padding: 24px 12px;
    position: absolute;
    left: var(--container-padding);
    top: 50%;
    transform: translateY(-50%);
    z-index: 10;
}

.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
}

.action-btn {
    width: 48px;
    height: 48px;
    border: none;
    background: white;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
    transition: all 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
    font-size: 20px;
    color: var(--text-color-secondary);
}

.action-btn:hover {
    transform: scale(1.15) translateY(-2px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
    color: var(--color-primary);
}

.action-btn.is-active {
    background: var(--color-primary);
    color: white;
    box-shadow: 0 8px 20px rgba(var(--color-primary-rgb), 0.3);
}

.action-btn.ai-btn.is-active {
    background: #8b5cf6;
    box-shadow: 0 8px 20px rgba(139, 92, 246, 0.3);
}

.action-label {
    font-size: 12px;
    color: var(--text-color-secondary);
    opacity: 0.8;
}

.article-right {
    width: 340px;
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
    height: 100%;
    transition: all 0.4s ease;
}

.article-right.expanded-panel {
    height: 100%;
    overflow: hidden;
    padding: 20px;
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

/* Transitions */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.3s ease;
}

.fade-slide-enter-from,
.fade-slide-leave-to {
    opacity: 0;
    transform: translateX(20px);
}
</style>
