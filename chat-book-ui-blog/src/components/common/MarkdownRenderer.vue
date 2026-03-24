<template>
    <RichTextViewer
        :html="renderedHtml"
        :variant="variant" />
</template>

<script setup>
import { computed } from 'vue';
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';
import { buildRichTextHtml } from '@/components/common/rich-text/content-pipeline.js';

const props = defineProps({
    content: {
        type: String,
        default: ''
    },
    sourceFormat: {
        type: String,
        default: 'markdown'
    },
    variant: {
        type: String,
        default: 'article'
    }
});

const renderedHtml = computed(() => buildRichTextHtml(props.content, props.sourceFormat));
</script>
