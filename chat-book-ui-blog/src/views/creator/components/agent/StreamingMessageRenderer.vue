<template>
    <div class="streaming-message-renderer">
        <RichTextViewer
            class="streaming-message-renderer__viewer"
            :html="renderedHtml"
            variant="chat" />
    </div>
</template>

<script setup>
import { onBeforeUnmount, ref, watch } from 'vue';

import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import { buildStreamingRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';

const STREAMING_RENDER_INTERVAL_MS = 56;

const props = defineProps({
    content: {
        type: String,
        default: ''
    }
});

const renderedHtml = ref('');
const pendingContent = ref('');
let renderTimer = null;
let lastRenderAt = 0;

const now = () => (
    typeof performance !== 'undefined' && typeof performance.now === 'function'
        ? performance.now()
        : Date.now()
);

const cancelScheduledRender = () => {
    if (renderTimer == null) {
        return;
    }
    clearTimeout(renderTimer);
    renderTimer = null;
};

const flushRender = () => {
    cancelScheduledRender();
    renderedHtml.value = buildStreamingRichTextHtml(pendingContent.value, 'markdown');
    lastRenderAt = now();
};

const scheduleRender = ({ immediate = false } = {}) => {
    if (immediate) {
        flushRender();
        return;
    }
    if (renderTimer != null) {
        return;
    }

    const elapsed = now() - lastRenderAt;
    const delay = Math.max(0, STREAMING_RENDER_INTERVAL_MS - elapsed);
    renderTimer = setTimeout(() => {
        renderTimer = null;
        flushRender();
    }, delay);
};

watch(() => props.content, (nextContent, previousContent) => {
    pendingContent.value = nextContent || '';
    const shouldFlushImmediately = !previousContent
        || !renderedHtml.value
        || pendingContent.value.length < String(previousContent || '').length
        || !pendingContent.value;
    scheduleRender({ immediate: shouldFlushImmediately });
}, {
    immediate: true
});

onBeforeUnmount(() => {
    cancelScheduledRender();
});
</script>

<style scoped>
.streaming-message-renderer {
    width: 100%;
    color: inherit;
}

.streaming-message-renderer__viewer {
    width: 100%;
}

.streaming-message-renderer :deep(.rich-text-viewer__body > :first-child) {
    margin-top: 0;
}

.streaming-message-renderer :deep(.rich-text-viewer__body > :last-child) {
    margin-bottom: 0;
}
</style>
