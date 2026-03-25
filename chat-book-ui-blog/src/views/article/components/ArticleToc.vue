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

<style scoped>
.article-toc {
  width: 100%;
  height: 100%;
  background: transparent;
  padding: 20px 0;
  overflow: hidden;
  /* Prevent overflow of child scroll wrapper */
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  /* Add border radius matching the editor */
  border-radius: var(--border-radius-xl, 16px);
}

/* Re-apply overflow to a wrapper to ensure smooth scrolling and border-radius clipping */
.toc-scroll-wrapper {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.toc-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 20px 12px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.05);
  /* Ensure header background matches and has top corners rounded */
  background: transparent;
  border-top-left-radius: inherit;
  border-top-right-radius: inherit;
}

.toc-title {
  font-weight: 600;
  color: #374151;
  font-size: 15px;
}

.toc-content {
  flex: 1;
  overflow-y: auto;
  padding-right: 12px;
  /* Match the border radius for the content area */
  border-bottom-left-radius: inherit;
  border-bottom-right-radius: inherit;
}

.toc-content::-webkit-scrollbar {
  width: 4px;
}

.toc-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.toc-content::-webkit-scrollbar-track {
  background: transparent;
  /* Ensure track doesn't overflow the rounded corners */
  border-radius: 4px;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.toc-item {
  font-size: 13px;
  color: #6b7280;
  cursor: pointer;
  padding: 8px 12px 8px 8px;
  /* Apply uniform border-radius to the list items as well */
  border-radius: var(--border-radius-base, 8px);
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
  border-left: 3px solid transparent;
  line-height: 1.4;
  margin-left: 8px;
  margin-right: 8px;
}

.toc-item:hover {
  background: rgba(0, 0, 0, 0.03);
  color: #374151;
}

.toc-item.active {
  color: #3b82f6;
  background: rgba(59, 130, 246, 0.05);
  font-weight: 500;
  border-left: 3px solid #3b82f6;
}

.level-1 {
  padding-left: 12px;
  font-weight: 600;
  color: #111827;
}

.level-2 {
  padding-left: 24px;
}

.level-3 {
  padding-left: 36px;
}

.level-4 {
  padding-left: 48px;
}

.level-5 {
  padding-left: 60px;
}

.level-6 {
  padding-left: 72px;
}

.empty-toc {
  text-align: center;
  color: #9ca3af;
  font-size: 13px;
  padding: 40px 0;
}
</style>
