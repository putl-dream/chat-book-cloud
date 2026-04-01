<template>
  <div class="article-toc">
    <div class="toc-header">
      <span class="toc-title">大纲</span>
    </div>

    <div class="toc-scroll-wrapper">
      <div class="toc-content">
        <div v-if="headings.length === 0" class="empty-toc">
          暂无大纲
        </div>
        <ul class="toc-list">
          <li v-for="(heading, index) in headings" :key="index" class="toc-item"
            :class="[`level-${heading.level}`, { 'active': activeId === heading.id }]"
            @click="scrollToHeading(heading.id)">
            {{ heading.text }}
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>

<script setup>
import { toRef, watch, nextTick } from 'vue';
import { useArticleToc } from '../_hooks/useArticleToc.js';

const props = defineProps({
  editor: {
    type: Object,
    required: true
  }
});

const editorRef = toRef(props, 'editor');
const { headings, activeId, scrollToHeading } = useArticleToc(editorRef);

// Auto-scroll the TOC sidebar to keep the highlighted item in view
watch(activeId, async (newId) => {
    if (!newId) return;
    await nextTick();
    const activeEl = document.querySelector(`.toc-item.active`);
    if (activeEl) {
        // Prevent scrollIntoView from bubbling up and hijacking the whole page
        const scrollContainer = activeEl.closest('.toc-content') || activeEl.closest('.toc-scroll-wrapper');
        if (scrollContainer) {
            const containerRect = scrollContainer.getBoundingClientRect();
            const elRect = activeEl.getBoundingClientRect();
            
            // Only scroll if the element is near the edges or outside the view
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
