<template>
    <div class="streaming-message-renderer">
        <span class="streaming-message-renderer__text">{{ content }}</span>
    </div>
</template>

<script setup>
defineProps({
    content: {
        type: String,
        default: ''
    }
});
</script>

<style scoped>
.streaming-message-renderer {
    width: 100%;
    color: inherit;
}

.streaming-message-renderer__text {
    display: block;
    white-space: pre-wrap;
    word-break: break-word;
    line-height: 1.72;
}
</style>
