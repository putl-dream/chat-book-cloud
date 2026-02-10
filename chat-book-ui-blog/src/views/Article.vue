<template>
    <div class="article-page">
        <div class="article-detail">
            <div class="left-sidebar">
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
            </div>

            <div class="content glass-panel">
                <header class="article-header">
                    <h1 class="article-title">{{ article.title }}</h1>
                    <div class="article-meta">
                        <div class="meta-item">
                            <span class="label">作者</span>
                            <span class="value">{{ article.userName }}</span>
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

            <div class="resize-handle" @mousedown="startResize"></div>

            <div class="article-right" :style="{ width: rightSidebarWidth + 'px' }"
                :class="{ 'expanded-panel glass-panel': activePanel !== 'default' }">
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

// 侧边栏宽度
const rightSidebarWidth = ref(300);
const isResizing = ref(false);

const startResize = () => {
    isResizing.value = true;
    document.addEventListener('mousemove', handleResize);
    document.addEventListener('mouseup', stopResize);
    document.body.style.cursor = 'col-resize';
    document.body.style.userSelect = 'none';
};

const handleResize = (e) => {
    if (!isResizing.value) return;
    const container = document.querySelector('.article-detail');
    if (container) {
        const containerRect = container.getBoundingClientRect();
        // 计算右侧宽度：容器右边界 - 鼠标X坐标 - 容器右padding
        let newWidth = containerRect.right - e.clientX - 24; // 24 is padding-right

        // 限制最小和最大宽度
        if (newWidth < 200) newWidth = 200;
        if (newWidth > 600) newWidth = 600;

        rightSidebarWidth.value = newWidth;
    }
};

const stopResize = () => {
    isResizing.value = false;
    document.removeEventListener('mousemove', handleResize);
    document.removeEventListener('mouseup', stopResize);
    document.body.style.cursor = '';
    document.body.style.userSelect = '';
};

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
    max-width: 1600px;
    margin: 0 auto;
    gap: 0;
    padding: 24px var(--container-padding);
    position: relative;
    align-items: stretch;
    height: 100%;
    overflow: hidden;
    box-sizing: border-box;
}

.left-sidebar {
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    flex-shrink: 0;
    width: 84px; /* 固定宽度 */
}

.resize-handle {
    width: 20px;
    margin: 0 -9px;
    cursor: col-resize;
    position: relative;
    z-index: 100;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-shrink: 0;
}

/* 视觉上的分割线 */
.resize-handle::after {
    content: '';
    width: 4px;
    height: 40px;
    background-color: rgba(0, 0, 0, 0.1);
    border-radius: 4px;
    transition: all 0.2s;
}

.resize-handle:hover::after,
.resize-handle:active::after {
    height: 100%;
    background-color: var(--color-primary);
    opacity: 0.5;
}

.content {
    flex: 1;
    min-width: 0;
    padding: 30px 50px;
    height: 100%;
    display: flex;
    flex-direction: column;
    margin-left: 0;
    /* 移除原来的margin */
}

.article-header {
    border-bottom: 1px solid rgba(0, 0, 0, 0.06);
    padding-bottom: 20px;
    margin-bottom: 20px;
}

.article-title {
    font-size: 1.8rem;
    font-weight: 700;
    color: var(--text-color-primary);
    line-height: 1.4;
    margin: 0 0 16px;
    letter-spacing: -0.01em;
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
    gap: 8px;
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
    position: relative;
    /* 改回 relative */
    left: auto;
    top: auto;
    transform: none;
    z-index: 10;
}

.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8px;
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
    /* width由JS控制 */
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
    height: 100%;
    transition: width 0.1s linear; /* 减少过渡时间使拖拽更跟手，或者移除过渡 */
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
