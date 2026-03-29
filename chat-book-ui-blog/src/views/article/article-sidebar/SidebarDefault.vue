<template>
    <div class="default-sidebar custom-scrollbar">
        <!-- Reading Progress Bar -->
        <div class="reading-progress-container" :class="{ 'is-visible': readingProgress > 0 }">
            <div class="reading-progress-bar" :style="{ width: readingProgress + '%' }"></div>
        </div>

        <div class="sidebar-module toc-module">
            <ArticleViewerToc :articleHtml="articleHtml" :contentTarget="contentTarget" />
        </div>

        <div class="sidebar-module">
            <ArticleTagCard :articleId="articleId" :tagIds="tagIds" />
        </div>

        <div class="sidebar-module">
            <RelatedCard :articleId="articleId" />
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue';
import ArticleViewerToc from "@/views/article/components/ArticleViewerToc.vue";
import ArticleTagCard from "@/views/article/components/ArticleTagCard.vue";
import RelatedCard from "@/views/article/components/RelatedCard.vue";

const props = defineProps({
    articleId: {
        type: [Number, String],
        required: true
    },
    tagIds: {
        type: Array,
        default: () => []
    },
    articleHtml: {
        type: String,
        default: ''
    },
    contentTarget: {
        type: Object,
        default: null
    }
});

const readingProgress = ref(0);

const calculateProgress = () => {
    if (!props.contentTarget) {
        readingProgress.value = 0;
        return;
    }
    
    // Calculate progress based on the main content element referenced by contentTarget
    const rect = props.contentTarget.getBoundingClientRect();
    const viewportHeight = window.innerHeight;
    
    // How much area can be scrolled theoretically
    const scrollableHeight = rect.height - viewportHeight;
    
    if (scrollableHeight <= 0) {
        // Content fits in viewport
        readingProgress.value = rect.top < 0 ? 100 : 0;
        return;
    }
    
    // Distance the content has scrolled past the top of viewport
    // Wait, the content starts with a top offset. When it scrolls up, rect.top goes negative.
    // If rect.top reaches -scrollableHeight, we are at the bottom.
    // Let's start progress when rect.top hits 0 (or some offset if there's a header).
    // The AppLayout header is 60px.
    const startOffset = 60; 
    
    const scrolled = startOffset - rect.top;
    
    if (scrolled <= 0) {
        readingProgress.value = 0;
    } else if (scrolled >= scrollableHeight + startOffset) {
        readingProgress.value = 100;
    } else {
        readingProgress.value = Math.min(100, Math.max(0, (scrolled / scrollableHeight) * 100));
    }
};

onMounted(() => {
    window.addEventListener('scroll', calculateProgress, { passive: true });
    window.addEventListener('resize', calculateProgress);
    // Initial calculation after dom layout
    setTimeout(calculateProgress, 100);
});

onUnmounted(() => {
    window.removeEventListener('scroll', calculateProgress);
    window.removeEventListener('resize', calculateProgress);
});
</script>

<style scoped>
.default-sidebar {
    display: flex;
    flex-direction: column;
    gap: 8px; /* Explicit gap between the sidebar modules */
    position: relative;
    padding-bottom: 20px;
}

.reading-progress-container {
    position: sticky;
    top: 0;
    left: 0;
    right: 0;
    height: 4px;
    background-color: var(--border-color-light, #f1f5f9);
    z-index: 10;
    margin-bottom: -4px; /* Overlay on top of the first element */
    opacity: 0;
    transition: opacity 0.3s ease;
}

.reading-progress-container.is-visible {
    opacity: 1;
}

.reading-progress-bar {
    height: 100%;
    background: linear-gradient(90deg, #60a5fa, #3b82f6);
    transition: width 0.15s ease-out;
    border-radius: 0 4px 4px 0;
}

.sidebar-module {
    display: block;
    width: 100%;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-xl);
    box-shadow: var(--box-shadow-base);
    border: 1px solid var(--border-color-light);
    overflow: hidden;
}



@media (max-width: 768px) {
    .sidebar-module:not(.toc-module) {
        display: none !important;
    }
}
</style>
