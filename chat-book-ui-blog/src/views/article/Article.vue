<template>
    <div class="article-page">
        <aside
            class="sidebar desktop-only"
            :class="{ 'is-collapsed': isSidebarCollapsed, 'is-expanded': isSidebarExpanded }"
            @mouseenter="handleSidebarHover(true)"
            @mouseleave="handleSidebarHover(false)"
            @click="handleSidebarClick">
            <div class="sidebar-inner">
                <div class="article-buttons">
                    <div class="action-item">
                        <el-button class="action-btn" size="large" circle :class="{ 'is-active': praiseStat !== 0 }" @click.stop="handleLike">
                            <el-icon><Pointer /></el-icon>
                        </el-button>
                        <span class="action-label" title="点赞">点赞</span>
                    </div>
                    <div class="action-item">
                        <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }" @click.stop="handleFavorite">
                            <el-icon><Star /></el-icon>
                        </el-button>
                        <span class="action-label" title="收藏">收藏</span>
                    </div>
                    <div class="action-item">
                        <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.COMMENT }" @click.stop="handleComment">
                            <el-icon><ChatLineRound /></el-icon>
                        </el-button>
                        <span class="action-label" title="评论">评论</span>
                    </div>

                    <div class="action-divider" aria-hidden="true"></div>

                    <div v-if="!isSelfAuthor" class="action-item">
                        <el-button
                            class="action-btn"
                            size="large"
                            circle
                            :loading="authorActionLoading"
                            :class="{ 'is-active': isFollowing }"
                            @click.stop="handleFollow">
                            <el-icon><UserFilled /></el-icon>
                        </el-button>
                        <span class="action-label" :title="followLabel">{{ followLabel }}</span>
                    </div>
                    <div class="action-item">
                        <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.AI }" @click.stop="handleAiChat">
                            <el-icon><Service /></el-icon>
                        </el-button>
                        <span class="action-label" title="AI 助手">AI</span>
                    </div>
                </div>
            </div>
        </aside>

        <!-- Mobile Bottom Toolbar -->
        <div class="mobile-toolbar mobile-only">
            <div class="action-item">
                <el-button class="action-btn" size="large" circle :class="{ 'is-active': praiseStat !== 0 }" @click.stop="handleLike">
                    <el-icon><Pointer /></el-icon>
                </el-button>
                <span class="action-label">点赞</span>
            </div>
            <div class="action-item">
                <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }" @click.stop="handleFavorite">
                    <el-icon><Star /></el-icon>
                </el-button>
                <span class="action-label">收藏</span>
            </div>
            <div class="action-item">
                <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.COMMENT }" @click.stop="handleComment">
                    <el-icon><ChatLineRound /></el-icon>
                </el-button>
                <span class="action-label">评论</span>
            </div>
            <div v-if="!isSelfAuthor" class="action-item">
                <el-button class="action-btn" size="large" circle :loading="authorActionLoading" :class="{ 'is-active': isFollowing }" @click.stop="handleFollow">
                    <el-icon><UserFilled /></el-icon>
                </el-button>
                <span class="action-label">{{ followLabel }}</span>
            </div>
            <div class="action-item">
                <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.AI }" @click.stop="handleAiChat">
                    <el-icon><Service /></el-icon>
                </el-button>
                <span class="action-label">AI</span>
            </div>
        </div>

        <div class="main-container">
            <div class="article-detail" :class="{ 'panel-open': showRightPanel }">
                <div ref="contentRef" class="content custom-scrollbar">
                    <header class="article-header">
                        <h1 class="article-title">{{ article.title }}</h1>

                        <div class="article-header-meta">
                            <div class="author-strip">
                                <el-avatar :size="42" :src="article.authorAvatar" class="author-avatar">
                                    {{ (article.userName || '作').slice(0, 1) }}
                                </el-avatar>
                                <div class="author-copy">
                                    <span class="author-kicker">Author</span>
                                    <span class="author-name">{{ article.userName || '作者' }}</span>
                                </div>
                            </div>

                            <div class="meta-cluster">
                                <button
                                    type="button"
                                    class="meta-panel-btn"
                                    :class="{ 'is-active': activePanel === PANEL_TYPE.DEFAULT && showRightPanel }"
                                    @click="openDefaultPanel">
                                    目录
                                </button>
                                <div class="article-meta">
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
                            </div>
                        </div>
                    </header>

                    <main class="article-content">
                        <div class="article-body">
                            <RichTextViewer :html="articleHtml" variant="article" :theme="articleTheme" as="article" />
                        </div>
                    </main>

                    <div v-if="isMobile" class="mobile-bottom-cards">
                        <ArticleTagCard :articleId="articleId" :tagIds="article.tagIds || []" />
                        <RelatedCard :articleId="articleId" />
                    </div>
                </div>

                <div v-if="showRightPanel && !isMobile" class="resize-handle" @mousedown="startResize"></div>

                <div
                    v-if="!isMobile"
                    class="article-right"
                    :style="{ width: effectiveRightWidth + 'px' }"
                    :class="{ 'expanded-panel': showRightPanel, 'glass-panel': showRightPanel }">
                    <transition name="fade-slide" mode="out-in">
                        <component
                            v-if="showRightPanel"
                            :is="activePanelComponent"
                            :key="activePanelKey"
                            v-bind="activePanelProps" />
                    </transition>
                </div>
            </div>
        </div>

        <el-drawer
            v-if="isMobile"
            v-model="showRightPanel"
            :title="activePanelTitle"
            direction="btt"
            size="70vh"
            class="mobile-panel-drawer"
            :append-to-body="true"
            destroy-on-close>
            <component
                :is="activePanelComponent"
                :key="activePanelKey"
                v-bind="activePanelProps" />
        </el-drawer>
    </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { ChatLineRound, Pointer, Service, Star, UserFilled } from '@element-plus/icons-vue';
import { useRoute } from "vue-router";
import RichTextViewer from "@/components/common/rich-text/RichTextViewer.vue";
import { buildRichTextHtml } from "@/components/common/rich-text/content-pipeline.js";
import SidebarDefault from '@/views/article/article-sidebar/SidebarDefault.vue';
import SidebarComment from '@/views/article/article-sidebar/SidebarComment.vue';
import SidebarAI from '@/views/article/article-sidebar/SidebarAI.vue';
import ArticleTagCard from "@/views/article/components/ArticleTagCard.vue";
import RelatedCard from "@/views/article/components/RelatedCard.vue";
import { useArticleLogic } from "./_hooks/useArticleLogic.js";
import { PANEL_TYPE } from "./_utils/config.js";
import { useSiteTheme } from '@/composables/useSiteTheme.js';

const route = useRoute();
const articleId = computed(() => route.params.id);
const contentRef = ref(null);

const componentMap = {
    [PANEL_TYPE.DEFAULT]: SidebarDefault,
    [PANEL_TYPE.COMMENT]: SidebarComment,
    [PANEL_TYPE.AI]: SidebarAI
};

const {
    article,
    praiseStat,
    collectStat,
    activePanel,
    showRightPanel,
    authorRelation,
    isSelfAuthor,
    authorActionLoading,
    rightSidebarWidth,
    startResize,
    queryArticleRequest,
    handleLike,
    handleComment,
    handleAiChat,
    handleFavorite,
    handleFollow,
    openDefaultPanel
} = useArticleLogic(articleId);

const articleHtml = computed(() => buildRichTextHtml(article.value?.content || '', 'html'));
const { articleTheme } = useSiteTheme();

const isFollowing = computed(() => authorRelation.value >= 0);
const followLabel = computed(() => {
    if (authorRelation.value === 1) {
        return '互关';
    }
    if (authorRelation.value === 0) {
        return '已关注';
    }
    return '关注';
});

const activePanelProps = computed(() => {
    const baseProps = {
        articleId: articleId.value,
        tagIds: article.value?.tagIds || []
    };

    if (activePanel.value === PANEL_TYPE.DEFAULT) {
        return {
            ...baseProps,
            articleHtml: articleHtml.value,
            contentTarget: contentRef.value
        };
    }

    return baseProps;
});

const activePanelComponent = computed(() => componentMap[activePanel.value] || SidebarDefault);
const activePanelKey = computed(() => `${activePanel.value}-${articleId.value}`);

const activePanelTitle = computed(() => {
    switch (activePanel.value) {
        case PANEL_TYPE.DEFAULT: return '目录';
        case PANEL_TYPE.COMMENT: return '评论';
        case PANEL_TYPE.AI: return 'AI 助手';
        default: return '阅读工具';
    }
});

watch(articleId, async (newId, oldId) => {
    if (!newId || newId === oldId) {
        return;
    }
    await queryArticleRequest();
}, { immediate: true });

const effectiveRightWidth = computed(() => (
    showRightPanel.value ? rightSidebarWidth.value : 0
));

const isSidebarCollapsed = ref(false);
const isSidebarExpanded = ref(false);
const isMobile = ref(false);

const checkViewport = () => {
    isSidebarCollapsed.value = window.innerWidth <= 1024;
    isMobile.value = window.innerWidth <= 768;
    if (!isSidebarCollapsed.value) {
        isSidebarExpanded.value = false;
    }
};

const handleSidebarHover = (isHovering) => {
    if (isSidebarCollapsed.value) {
        isSidebarExpanded.value = isHovering;
    }
};

const handleSidebarClick = () => {
    if (isSidebarCollapsed.value) {
        isSidebarExpanded.value = !isSidebarExpanded.value;
    }
};

onMounted(() => {
    checkViewport();
    window.addEventListener('resize', checkViewport);
});

onUnmounted(() => {
    window.removeEventListener('resize', checkViewport);
});
</script>

<style scoped>
.article-page {
    display: grid;
    grid-template-columns: 10% 1fr;
    min-height: 100%;
    background: var(--article-page-radial), var(--article-page-bg);
    position: relative;
}

.sidebar {
    position: sticky;
    top: 60px;
    height: calc(100vh - 60px);
    transition: width 0.3s ease, transform 0.3s ease, background-color 0.3s ease;
    display: flex;
    flex-direction: column;
    justify-content: center;
    box-sizing: border-box;
    z-index: 20;
    overflow: hidden;
}

.sidebar-inner {
    width: 70%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: width 0.3s ease;
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
    align-items: center;
}

.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    width: 100%;
    max-width: 100%;
    overflow: hidden;
}

.action-divider {
    width: 30px;
    height: 1px;
    background: linear-gradient(90deg, rgba(15, 23, 42, 0), rgba(15, 23, 42, 0.22), rgba(15, 23, 42, 0));
}

.action-btn {
    width: 40px;
    height: 40px;
    border: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.92);
    box-shadow: none;
    transition: color 0.2s, background 0.2s, border-color 0.2s, transform 0.2s;
    font-size: 18px;
    color: var(--text-color-secondary);
    flex-shrink: 0;
}

.action-btn:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    transform: translateY(-1px);
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
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    text-align: center;
}

.main-container {
    min-height: 100%;
    position: relative;
    display: flex;
    flex-direction: column;
    box-sizing: border-box;
    transition: padding-left 0.3s ease;
}

.article-detail {
    display: flex;
    width: 100%;
    max-width: 1600px;
    margin: 0 auto;
    gap: 0;
    padding: var(--container-padding) var(--container-padding) 0 0;
    align-items: stretch;
    min-height: 100%;
    box-sizing: border-box;
}

.article-detail.panel-open .content {
    margin-left: 0;
    margin-right: auto;
}

.content {
    min-width: 0;
    flex: 1;
    margin: 0 auto;
    padding: 32px clamp(18px, 3vw, 44px) 36px;
    display: flex;
    flex-direction: column;
    background: var(--article-paper-bg);
    border-radius: 20px 20px 0 0;
    border: 1px solid var(--article-paper-border);
    box-shadow: var(--article-paper-shadow);
    scroll-behavior: smooth;
    scroll-padding-top: 28px;
}

.article-header {
    border-bottom: 1px solid var(--article-header-divider);
    padding-bottom: 18px;
    margin: 0 0 18px;
    width: min(100%, 900px);
    margin-inline: auto;
}

.article-title {
    font-size: clamp(1.74rem, 1.62rem + 0.48vw, 2.08rem);
    font-weight: 700;
    font-family: "Iowan Old Style", "Palatino Linotype", "Source Han Serif SC", "Songti SC", serif;
    color: var(--article-title-color);
    line-height: 1.18;
    margin: 0 0 16px;
    letter-spacing: -0.03em;
}

.article-header-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    gap: 18px 24px;
    flex-wrap: wrap;
}

.author-strip {
    display: inline-flex;
    align-items: center;
    gap: 12px;
    min-width: 0;
}

.author-avatar {
    border: 1px solid rgba(15, 23, 42, 0.08);
    box-shadow: 0 12px 30px rgba(15, 23, 42, 0.08);
    flex-shrink: 0;
}

.author-copy {
    display: flex;
    flex-direction: column;
    gap: 2px;
    min-width: 0;
}

.author-kicker {
    font-size: 0.68rem;
    text-transform: uppercase;
    letter-spacing: 0.22em;
    color: var(--text-color-secondary);
    opacity: 0.7;
}

.author-name {
    font-size: 1rem;
    font-weight: 700;
    color: var(--article-meta-value);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.meta-cluster {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-wrap: wrap;
    justify-content: flex-end;
}

.meta-panel-btn {
    border: 1px solid rgba(15, 23, 42, 0.1);
    background: rgba(255, 255, 255, 0.84);
    color: var(--text-color-secondary);
    padding: 8px 14px;
    border-radius: 999px;
    cursor: pointer;
    transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
    font-size: 0.82rem;
    font-weight: 600;
}

.meta-panel-btn:hover,
.meta-panel-btn.is-active {
    background: rgba(59, 130, 246, 0.1);
    color: var(--color-primary);
    border-color: rgba(59, 130, 246, 0.18);
}

.article-meta {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 12px;
    font-size: 0.8125rem;
    color: var(--article-meta-color);
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
    color: var(--article-meta-value);
}

.meta-divider {
    width: 1px;
    height: 12px;
    background: var(--article-meta-divider);
}

.article-content {
    flex: 1;
    width: 100%;
    min-height: 0;
}

.article-body {
    width: 100%;
    min-height: 100%;
    padding-bottom: 28px;
    font-size: 16px;
    line-height: 1.7;
}

.article-body :deep(pre), 
.article-body :deep(code) {
    overflow-x: auto;
    white-space: pre;
    max-width: 100%;
}

.mobile-bottom-cards {
    display: flex;
    flex-direction: column;
    gap: 16px;
    margin-top: 32px;
    padding-top: 24px;
    border-top: 1px solid var(--border-color-light);
}

.resize-handle {
    width: 6px;
    margin: 0 -3px;
    cursor: col-resize;
    position: sticky;
    top: 60px;
    height: calc(100vh - 60px);
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
    position: sticky;
    height: calc(100vh - 60px);
    overflow-y: auto;
    transition: width 0.25s ease;
}

.article-right.expanded-panel {
    height: calc(100vh - 60px);
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
    background: var(--article-scrollbar);
    border-radius: 10px;
}

.custom-scrollbar::-webkit-scrollbar-thumb:hover {
    background: var(--article-scrollbar-hover);
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

/* Mobile toolbar - hidden by default */
.mobile-only {
    display: none !important;
}

.desktop-only {
    display: flex;
}

@media (max-width: 1024px) {
    .article-page {
        display: block;
    }

    .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        width: 60px;
        background-color: rgba(255, 255, 255, 0.8);
        backdrop-filter: blur(8px);
        border-right: 1px solid rgba(0, 0, 0, 0.05);
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
        cursor: pointer;
    }

    .sidebar.is-collapsed .sidebar-inner {
        width: 100%;
    }

    .sidebar.is-expanded {
        width: 70%;
        background-color: #fff;
        box-shadow: 4px 0 16px rgba(0, 0, 0, 0.1);
        cursor: default;
    }

    .sidebar.is-expanded .sidebar-inner {
        width: 70%;
    }

    .main-container {
        padding-left: 60px;
    }
}

@media (max-width: 900px) {
    .article-detail {
        padding-inline: 12px;
    }

    .content {
        padding-inline: 18px;
    }

    .article-header-meta,
    .meta-cluster {
        align-items: flex-start;
        justify-content: flex-start;
    }
}

@media (max-width: 768px) {
    .mobile-only {
        display: flex !important;
    }

    .desktop-only {
        display: none !important;
    }

    .mobile-toolbar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 60px;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        justify-content: space-around;
        align-items: center;
        z-index: 100;
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding: 0 8px;
    }

    .mobile-toolbar .action-item {
        flex: 1;
        max-width: 70px;
    }

    .mobile-toolbar .action-btn {
        width: 36px;
        height: 36px;
        font-size: 16px;
    }

    .mobile-toolbar .action-label {
        font-size: 10px;
    }

    .article-page {
        padding-bottom: 70px;
    }

    .main-container {
        padding-left: 0;
    }

    .article-detail {
        padding-inline: 0;
    }

    .content {
        padding: 20px 16px 36px;
        border-radius: 16px 16px 0 0;
    }

    .article-header {
        padding-bottom: 14px;
        margin-bottom: 14px;
        width: 100%;
    }

    .article-title {
        font-size: 1.5rem;
        margin-bottom: 12px;
    }
}
</style>
