<template>
    <div class="viewer-toc">
        <div class="toc-header">
            <span class="toc-kicker">Reader Tools</span>
            <span class="toc-title">目录</span>
        </div>

        <div class="toc-content">
            <div v-if="headings.length === 0" class="empty-toc">暂无目录</div>
            <ul v-else class="toc-list">
                <li
                    v-for="heading in headings"
                    :key="heading.id"
                    class="toc-item"
                    :class="[`level-${heading.level}`, { 'is-active': activeId === heading.id }]"
                    @click="scrollToHeading(heading.id)">
                    <span class="toc-item-text">{{ heading.text }}</span>
                </li>
            </ul>
        </div>
    </div>
</template>

<script setup>
import { toRef } from 'vue';
import { useArticleViewerToc } from '../_hooks/useArticleViewerToc.js';

const props = defineProps({
    articleHtml: {
        type: String,
        default: ''
    },
    contentTarget: {
        type: Object,
        default: null
    }
});

const articleHtmlRef = toRef(props, 'articleHtml');
const contentTargetRef = toRef(props, 'contentTarget');
const { headings, activeId, scrollToHeading } = useArticleViewerToc(articleHtmlRef, contentTargetRef);
</script>

<style scoped>
.viewer-toc {
    display: flex;
    flex-direction: column;
    min-height: 220px;
}

.toc-header {
    padding: 18px 20px 14px;
    border-bottom: 1px solid rgba(15, 23, 42, 0.08);
    display: flex;
    flex-direction: column;
    gap: 4px;
}

.toc-kicker {
    font-size: 0.68rem;
    letter-spacing: 0.18em;
    text-transform: uppercase;
    color: var(--text-color-secondary);
    opacity: 0.75;
}

.toc-title {
    font-size: 1rem;
    font-weight: 700;
    color: var(--text-color-primary);
}

.toc-content {
    max-height: min(42vh, 420px);
    overflow-y: auto;
    padding: 12px 10px 16px;
}

.toc-content::-webkit-scrollbar {
    width: 4px;
}

.toc-content::-webkit-scrollbar-thumb {
    background: rgba(15, 23, 42, 0.14);
    border-radius: 999px;
}

.toc-list {
    list-style: none;
    padding: 0;
    margin: 0;
}

.toc-item {
    display: flex;
    align-items: flex-start;
    gap: 10px;
    padding: 9px 12px;
    margin: 0 4px 4px;
    border-radius: 12px;
    color: var(--text-color-secondary);
    cursor: pointer;
    transition: background-color 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.toc-item::before {
    content: '';
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-top: 0.5em;
    flex-shrink: 0;
    background: currentColor;
    opacity: 0.28;
}

.toc-item:hover {
    background: rgba(59, 130, 246, 0.06);
    color: var(--article-meta-value);
    transform: translateX(2px);
}

.toc-item.is-active {
    background: rgba(59, 130, 246, 0.1);
    color: var(--color-primary);
}

.toc-item.is-active::before {
    opacity: 1;
}

.toc-item-text {
    min-width: 0;
    line-height: 1.45;
    font-size: 0.84rem;
    word-break: break-word;
}

.level-1 {
    font-weight: 700;
}

.level-2 {
    padding-left: 22px;
}

.level-3 {
    padding-left: 34px;
}

.level-4,
.level-5,
.level-6 {
    padding-left: 46px;
}

.empty-toc {
    padding: 48px 18px;
    text-align: center;
    color: var(--text-color-secondary);
    font-size: 0.84rem;
}
</style>
