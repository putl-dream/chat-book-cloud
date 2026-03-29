<template>
    <div class="article-page">
        <ArticleSidebar :praiseStat="praiseStat" :collectStat="collectStat" :activePanel="activePanel"
            :showRightPanel="showRightPanel" :isSelfAuthor="isSelfAuthor" :authorActionLoading="authorActionLoading"
            :isFollowing="isFollowing" :followLabel="followLabel" :isSidebarCollapsed="isSidebarCollapsed"
            :isSidebarExpanded="isSidebarExpanded" @handleLike="handleLike" @handleFavorite="handleFavorite"
            @handleComment="handleComment" @handleFollow="handleFollow" @handleAiChat="handleAiChat"
            @handleSidebarHover="handleSidebarHover" @handleSidebarClick="handleSidebarClick" />

        <!-- Mobile Bottom Toolbar -->
        <ArticleMobileToolbar :praiseStat="praiseStat" :collectStat="collectStat" :activePanel="activePanel"
            :showRightPanel="showRightPanel" :isSelfAuthor="isSelfAuthor" :authorActionLoading="authorActionLoading"
            :isFollowing="isFollowing" :followLabel="followLabel" @handleLike="handleLike"
            @handleFavorite="handleFavorite" @handleComment="handleComment" @handleFollow="handleFollow"
            @handleAiChat="handleAiChat" />

        <div class="main-container">
            <div class="article-detail" :class="{ 'panel-open': showRightPanel }">
                <div ref="contentRef" class="content custom-scrollbar">
                    <ArticleHeader :article="article" :activePanel="activePanel" :showRightPanel="showRightPanel"
                        @openDefaultPanel="openDefaultPanel" />

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

                <div v-if="!isMobile" class="article-right custom-scrollbar" :style="{ width: effectiveRightWidth + 'px' }"
                    :class="{ 'expanded-panel': showRightPanel, 'glass-panel': showRightPanel }">
                    <transition name="fade-slide" mode="out-in">
                        <component v-if="showRightPanel" :is="activePanelComponent" :key="activePanelKey"
                            v-bind="activePanelProps" />
                    </transition>
                </div>
            </div>
        </div>

        <el-drawer v-if="isMobile" v-model="showRightPanel" :title="activePanelTitle" direction="btt" size="70vh"
            class="mobile-panel-drawer" :append-to-body="true" destroy-on-close>
            <component :is="activePanelComponent" :key="activePanelKey" v-bind="activePanelProps" />
        </el-drawer>
    </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue';
import { useRoute } from "vue-router";
import RichTextViewer from "@/components/common/rich-text/RichTextViewer.vue";
import { buildRichTextHtml } from "@/components/common/rich-text/content-pipeline.js";
import SidebarDefault from '@/views/article/article-sidebar/SidebarDefault.vue';
import SidebarComment from '@/views/article/article-sidebar/SidebarComment.vue';
import SidebarAI from '@/views/article/article-sidebar/SidebarAI.vue';
import ArticleTagCard from "@/views/article/components/ArticleTagCard.vue";
import RelatedCard from "@/views/article/components/RelatedCard.vue";
import ArticleSidebar from "@/views/article/components/ArticleSidebar.vue";
import ArticleMobileToolbar from "@/views/article/components/ArticleMobileToolbar.vue";
import ArticleHeader from "@/views/article/components/ArticleHeader.vue";
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
    display: grid !important;
    grid-template-columns: 10% 1fr;
    min-height: 100%;
    background: var(--article-page-radial), var(--article-page-bg);
    position: relative;
    --scrollbar-thumb: var(--article-scrollbar);
    --scrollbar-thumb-hover: var(--article-scrollbar-hover);
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
    top: 60px;
    height: calc(100vh - 60px);
    overflow-y: auto;
    transition: width 0.25s ease;
}

.article-right.expanded-panel {
    height: calc(100vh - 60px);
    padding: 0 12px;
    min-width: 0;
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

@media (max-width: 1024px) {
    .article-page {
        display: block !important;
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
}

@media (max-width: 768px) {
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
}
</style>
