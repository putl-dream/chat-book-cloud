<template>
    <RichTextViewer
        :content="content"
        :source-format="sourceFormat"
        :variant="variant" />
</template>

<script setup>
import RichTextViewer from '@/components/common/rich-text/RichTextViewer.vue';

defineProps({
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
</script>
