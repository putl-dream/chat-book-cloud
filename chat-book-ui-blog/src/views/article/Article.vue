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
                        <ArticleTagCard :articleId="articleId" :author-tags="article.authorTags || []" />
                        <RelatedCard :articleId="articleId" />
                    </div>
                </div>

                <div v-if="showRightPanel && !isMobile" class="resize-handle" @mousedown="startResize"></div>

                <div v-if="!isMobile" class="article-right custom-scrollbar" :style="{ width: effectiveRightWidth + 'px' }"
                    :class="{ 'expanded-panel': showRightPanel, 'glass-panel': showRightPanel }">
                    <transition name="article-fade-slide" mode="out-in">
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
        authorTags: article.value?.authorTags || []
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
