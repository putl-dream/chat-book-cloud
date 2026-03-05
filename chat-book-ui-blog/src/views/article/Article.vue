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

            <div class="content">
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
import { ElButton, ElMessage } from 'element-plus';
import MarkdownRenderer from "@/components/common/MarkdownRenderer.vue";

import SidebarDefault from '@/views/article/article-sidebar/SidebarDefault.vue';
import SidebarComment from '@/views/article/article-sidebar/SidebarComment.vue';
import SidebarAI from '@/views/article/article-sidebar/SidebarAI.vue';
import { useArticleLogic } from "./Article/_hooks/useArticleLogic.js";

const route = useRoute();
const articleId = route.params.id;

const componentMap = {
    'default': SidebarDefault,
    'comment': SidebarComment,
    'ai': SidebarAI
};

const {
    article,
    praiseStat,
    collectStat,
    activePanel,
    rightSidebarWidth,
    startResize,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite
} = useArticleLogic(articleId);

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
    width: 84px;
    /* 固定宽度 */
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
    padding: 40px 60px;
    height: 100%;
    display: flex;
    flex-direction: column;
    background: transparent;
    border: none;
    box-shadow: none;
}

.article-header {
    border-bottom: 1px solid var(--border-color-base);
    padding-bottom: 24px;
    margin: 0 auto 32px;
    max-width: 860px;
    width: 100%;
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
    font-size: 1.125rem;
    line-height: 1.8;
    color: var(--text-color-regular);
    padding-right: 16px;
    max-width: 860px;
    width: 100%;
    margin: 0 auto;
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 32px;
    height: fit-content;
    padding: 24px 16px;
    z-index: 10;
    background: var(--bg-color-glass);
    backdrop-filter: var(--blur-base);
    border: 1px solid var(--border-color-glass);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-glass);
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
    transition: width 0.1s linear;
    /* 减少过渡时间使拖拽更跟手，或者移除过渡 */
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
