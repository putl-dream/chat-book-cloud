<template>
    <div class="article-page">
        <!-- 左侧固定按钮组：点赞、评论、收藏（不占布局空间） -->
        <aside :class="['article-buttons-fixed', { 'with-right-panel': showRightPanel }]">
            <div class="article-buttons">
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
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }"
                        @click="handleFavorite">
                        <el-icon>
                            <Star />
                        </el-icon>
                    </el-button>
                    <span class="action-label">收藏</span>
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
            </div>
        </aside>

        <!-- 右下角 AI 助手悬浮球（展开时上移避免与发送按钮重叠） -->
        <button type="button" class="fab-ai" :class="{ 'is-active': activePanel === 'ai' }" @click="handleAiChat"
            title="AI 助手">
            <el-icon>
                <Service />
            </el-icon>
        </button>

        <div class="article-detail" :class="{ 'panel-open': showRightPanel }">
            <div class="content">
                <header class="article-header">
                    <h1 class="article-title">{{ article.title }}</h1>
                    <div class="article-meta">
                        <div class="meta-item">
                            <span class="label">作者</span>
                            <span class="value meta-author" @click="openAuthorPanel">{{ article.userName }}</span>
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

            <!-- 仅在有侧边面板时显示拖拽条（一条缝） -->
            <div v-if="showRightPanel" class="resize-handle" @mousedown="startResize"></div>

            <div class="article-right" :style="{ width: effectiveRightWidth + 'px' }"
                :class="{ 'expanded-panel glass-panel': showRightPanel }">
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
import { computed, onMounted, ref } from 'vue';
import { ChatLineRound, Pointer, Star, Service } from '@element-plus/icons-vue';
import { useRoute } from "vue-router";
import { ElButton } from 'element-plus';
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
    showRightPanel,
    rightSidebarWidth,
    startResize,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite,
    openAuthorPanel
} = useArticleLogic(articleId);

/** 右侧展开时显示可拖拽宽度，否则为 0 */
const effectiveRightWidth = computed(() =>
    showRightPanel.value ? rightSidebarWidth.value : 0
);

onMounted(() => {
    queryArticleRequest();
});
</script>

<style scoped>
.article-page {
    height: 100%;
    background-color: #f9fafb;
    position: relative;
}

.article-detail {
    display: flex;
    max-width: 1600px;
    margin: 0 auto;
    gap: 0;
    padding: 24px var(--container-padding);
    padding-left: 92px;
    /* 为左侧固定按钮留空，避免与文章重合 */
    position: relative;
    align-items: stretch;
    height: 100%;
    overflow: hidden;
    box-sizing: border-box;
}

.article-detail.panel-open .content {
    margin-left: 0;
    margin-right: auto;
}

/* 左侧固定按钮组：不占布局，轻量化
   - 默认：贴近内容区域（略靠中）
   - 打开右侧面板时：稍向左移，避免与内容重合 */
.article-buttons-fixed {
    position: fixed;
    left: 150px;
    top: 50%;
    transform: translateY(-50%);
    z-index: 20;
}

.article-buttons-fixed.with-right-panel {
    left: 20px;
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 24px;
    padding: 16px 12px;
    background: transparent;
    border: none;
    border-radius: var(--border-radius-large);
}

.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
}

.action-btn {
    width: 40px;
    height: 40px;
    border: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.9);
    box-shadow: none;
    transition: color 0.2s, background 0.2s, border-color 0.2s;
    font-size: 18px;
    color: var(--text-color-secondary);
}

.action-btn:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    background: rgba(255, 255, 255, 1);
}

.action-btn.is-active {
    background: var(--color-primary);
    color: white;
    border-color: var(--color-primary);
}

.action-label {
    font-size: 11px;
    color: var(--text-color-secondary);
    opacity: 0.85;
}

/* 右下角 AI 悬浮球；展开时上移避免与 AI 区域发送按钮重叠 */
.fab-ai {
    position: fixed;
    right: 32px;
    bottom: 32px;
    z-index: 20;
    width: 56px;
    height: 56px;
    border-radius: 50%;
    border: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.95);
    color: var(--text-color-secondary);
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
    transition: color 0.2s, background 0.2s, transform 0.2s, bottom 0.25s ease;
}

.fab-ai:hover {
    color: #8b5cf6;
    background: #fff;
    transform: scale(1.05);
}

.fab-ai.is-active {
    background: #8b5cf6;
    color: white;
    border-color: #8b5cf6;
    bottom: 160px;
}

.meta-author {
    cursor: pointer;
    text-decoration: underline;
    text-underline-offset: 2px;
}

.meta-author:hover {
    color: var(--color-primary);
}

/* 中间内容区：约 60%–70% 宽度，纸张感；右侧抽屉展开时贴左以缩小空隙 */
.content {
    min-width: 0;
    max-width: 1000px;
    margin: 0 auto;
    padding: 40px 56px 48px;
    height: 100%;
    display: flex;
    flex-direction: column;
    background: #ffffff;
    border-radius: 8px;
    border: 1px solid rgba(0, 0, 0, 0.06);
    box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.article-header {
    border-bottom: 1px solid var(--border-color-base);
    padding-bottom: 24px;
    margin: 0 0 32px;
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

@media (min-width: 1200px) {
    .article-title {
        font-size: 2.25rem;
    }
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
    padding-right: 12px;
    width: 100%;
}

/* 展开右侧时显示一条缝的拖拽条，减小与文章区域的间隙 */
.resize-handle {
    width: 6px;
    margin: 0 -3px;
    cursor: col-resize;
    position: relative;
    z-index: 100;
    display: flex;
    justify-content: center;
    align-items: center;
    flex-shrink: 0;
}

.resize-handle::after {
    content: '';
    width: 2px;
    height: 40px;
    background-color: rgba(0, 0, 0, 0.08);
    border-radius: 1px;
    transition: all 0.2s;
}

.resize-handle:hover::after,
.resize-handle:active::after {
    height: 100%;
    background-color: var(--color-primary);
    opacity: 0.6;
    width: 3px;
}

.article-right {
    flex-shrink: 0;
    display: flex;
    flex-direction: column;
    gap: 8px;
    height: 100%;
    overflow: hidden;
    transition: width 0.25s ease;
}

.article-right.expanded-panel {
    height: 100%;
    overflow: hidden;
    padding: 0 12px;
    min-width: 0;
}

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
