<template>
    <div class="message-wrapper" :class="{ 'self': message.sender === 'self' }">
        <div class="message-container">
            <el-avatar :size="36" :src="message.avatar" class="message-avatar" />
            <div class="message-content-wrapper">
                <div class="message-bubble">
                    <div class="message-text">{{ message.content }}</div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    message: {
        type: Object,
        required: true
    }
})
</script>

<style scoped>
.message-wrapper {
    margin-bottom: 24px;
    display: flex;
    flex-direction: column;
}

.message-container {
    display: flex;
    align-items: flex-start;
    gap: 12px;
}

.message-wrapper.self .message-container {
    flex-direction: row-reverse;
}

.message-avatar {
    flex-shrink: 0;
    box-shadow: var(--box-shadow-base);
    border-radius: var(--border-radius-base);
}

.message-content-wrapper {
    max-width: 70%;
    display: flex;
    flex-direction: column;
}

.message-bubble {
    padding: 12px 16px;
    border-radius: var(--border-radius-large);
    font-size: 14px;
    line-height: 1.5;
    position: relative;
    transition: var(--transition-fast);
}

/* Other's Message */
.message-bubble {
    background: var(--bg-color-white);
    color: var(--text-color-regular);
    border: 1px solid var(--border-color-base);
    box-shadow: var(--box-shadow-base);
    border-top-left-radius: 4px;
}

/* Self's Message */
.message-wrapper.self .message-bubble {
    background: var(--color-primary);
    color: #ffffff;
    border-color: var(--color-primary);
    border-top-left-radius: var(--border-radius-large);
    border-top-right-radius: 4px;
}

.message-text {
    word-break: break-word;
    white-space: pre-wrap;
}

.message-bubble:hover {
    box-shadow: var(--box-shadow-hover);
}
</style>
