<template>
    <div class="friend-item" :class="{ 'active': active }">
        <el-avatar :size="48" :src="friend.photo" class="friend-avatar" />
        <div class="friend-info">
            <div class="friend-name-status">
                <span class="friend-name">{{ friend.username }}</span>
                <span class="friend-status" :class="{ 'online': friend.status === '在线' }">{{ friend.status || '离线'
                    }}</span>
            </div>
            <div class="friend-last-message">
                <span class="last-message" v-if="friend.lastChat">{{ friend.lastChat }}</span>
                <span class="last-message" v-else>{{ friend.profile || '暂无简介' }}</span>
                <span class="last-message-time" v-if="friend.lastTime">{{ friend.lastTime }}</span>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    friend: {
        type: Object,
        required: true
    },
    active: {
        type: Boolean,
        default: false
    }
})
</script>
