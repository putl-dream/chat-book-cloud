<template>
  <div class="article-toc">
    <div class="toc-header">
      <span class="toc-title">大纲</span>
    </div>

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
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue';

const props = defineProps({
  editor: {
    type: Object,
    required: true
  }
});

const headings = ref([]);
const activeId = ref('');

// Generate unique ID for headings if not present
const updateHeadings = () => {
  if (!props.editor) return;

  const transaction = props.editor.state.tr;
  let modified = false;
  const newHeadings = [];

  props.editor.state.doc.descendants((node, pos) => {
    if (node.type.name === 'heading') {
      const id = node.attrs.id || `heading-${Math.random().toString(36).substring(2, 9)}`;

      // If node doesn't have ID, add it
      if (!node.attrs.id) {
        transaction.setNodeMarkup(pos, undefined, { ...node.attrs, id });
        modified = true;
      }

      newHeadings.push({
        level: node.attrs.level,
        text: node.textContent,
        id: id,
        pos: pos
      });
    }
  });

  if (modified) {
    props.editor.view.dispatch(transaction);
  }

  headings.value = newHeadings;
};

const scrollToHeading = (id) => {
  if (!props.editor) return;

  const element = props.editor.view.dom.querySelector(`[id="${id}"]`);
  if (element) {
    element.scrollIntoView({ behavior: 'smooth', block: 'start' });
    activeId.value = id;
  }
};

// Watch for editor updates
watch(() => props.editor, (newEditor) => {
  if (newEditor) {
    newEditor.on('update', updateHeadings);
    // Initial update
    updateHeadings();
  }
}, { immediate: true });

onBeforeUnmount(() => {
  if (props.editor) {
    props.editor.off('update', updateHeadings);
  }
});
</script>

<style scoped>
.article-toc {
  width: 240px;
  height: 100%;
  background: var(--bg-color-glass);
  backdrop-filter: var(--blur-base);
  border-right: 1px solid var(--border-color-glass);
  padding: 20px 0;
  overflow-y: auto;
  flex-shrink: 0;
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
  border-radius: 0 4px 4px 0;
  transition: all 0.2s;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 2px;
  border-left: 3px solid transparent;
  line-height: 1.4;
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
