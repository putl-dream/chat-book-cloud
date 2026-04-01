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
import { toRef, watch, nextTick } from 'vue';
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

watch(activeId, async (newId) => {
    if (!newId) return;
    await nextTick();
    const activeEl = document.querySelector('.viewer-toc .toc-item.is-active');
    if (activeEl) {
        const scrollContainer = activeEl.closest('.toc-content');
        if (scrollContainer) {
            const containerRect = scrollContainer.getBoundingClientRect();
            const elRect = activeEl.getBoundingClientRect();
            
            if (elRect.top < containerRect.top + 20 || elRect.bottom > containerRect.bottom - 20) {
                const targetScrollTop = scrollContainer.scrollTop + (elRect.top - containerRect.top) - (containerRect.height / 2) + (elRect.height / 2);
                scrollContainer.scrollTo({
                    top: targetScrollTop,
                    behavior: 'smooth'
                });
            }
        }
    }
});
</script>
