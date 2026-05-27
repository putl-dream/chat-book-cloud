<template>
    <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>
        <div v-if="metaBadges.length > 0" class="article-badges">
            <span v-for="badge in metaBadges" :key="badge" class="article-badge">{{ badge }}</span>
        </div>

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
                <button type="button" class="meta-panel-btn"
                    :class="{ 'is-active': activePanel === PANEL_TYPE.DEFAULT && showRightPanel }"
                    @click="$emit('openDefaultPanel')">
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
</template>

<script setup>
import { computed } from 'vue';
import { ARTICLE_TYPE_NAMES, CREATION_STATEMENT_NAMES } from '@/constants';
import { PANEL_TYPE } from "../_utils/config.js";

const props = defineProps({
    article: { type: Object, default: () => ({}) },
    activePanel: { type: String, default: '' },
    showRightPanel: { type: Boolean, default: false }
});

const emit = defineEmits(['openDefaultPanel']);

const metaBadges = computed(() => {
    const badges = [];

    if (props.article?.articleType && ARTICLE_TYPE_NAMES[props.article.articleType]) {
        badges.push(ARTICLE_TYPE_NAMES[props.article.articleType]);
    }

    const statements = Array.isArray(props.article?.creationStatements)
        ? props.article.creationStatements
        : [];
    statements.forEach((statement) => {
        if (CREATION_STATEMENT_NAMES[statement]) {
            badges.push(CREATION_STATEMENT_NAMES[statement]);
        }
    });

    return badges;
});
</script>

<style scoped>
.article-badges {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
    margin: 14px 0 20px;
}

.article-badge {
    display: inline-flex;
    align-items: center;
    padding: 6px 12px;
    border-radius: 999px;
    background: rgba(var(--color-success-rgb, 16, 185, 129), 0.08);
    color: var(--color-success);
    font-size: 12px;
    font-weight: 600;
}
</style>
