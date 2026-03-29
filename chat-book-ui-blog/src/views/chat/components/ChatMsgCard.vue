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
