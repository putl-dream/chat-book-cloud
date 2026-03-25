<template>
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
import { PANEL_TYPE } from "../_utils/config.js";

const props = defineProps({
    article: { type: Object, default: () => ({}) },
    activePanel: { type: String, default: '' },
    showRightPanel: { type: Boolean, default: false }
});

const emit = defineEmits(['openDefaultPanel']);
</script>

<style scoped>
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

@media (max-width: 900px) {

    .article-header-meta,
    .meta-cluster {
        align-items: flex-start;
        justify-content: flex-start;
    }
}

@media (max-width: 768px) {
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
