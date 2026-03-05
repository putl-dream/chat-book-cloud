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

<style scoped>
.friend-item {
    display: flex;
    padding: 16px;
    margin: 8px 12px;
    border-radius: var(--border-radius-large);
    transition: var(--transition-base);
    cursor: pointer;
    background: transparent;
    border: 1px solid transparent;
}

.friend-item:hover {
    background: var(--bg-color-light);
    transform: translateY(-1px);
}

.friend-item.active {
    background: var(--color-primary-light);
    border-color: var(--color-primary-light);
}

.friend-item.active .friend-name {
    color: var(--color-primary);
    font-weight: 600;
}

.friend-avatar {
    border-radius: var(--border-radius-base);
    box-shadow: var(--box-shadow-base);
}

.friend-info {
    flex: 1;
    margin-left: 14px;
    display: flex;
    flex-direction: column;
    justify-content: center;
    overflow: hidden;
}

.friend-name-status {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 4px;
}

.friend-name {
    font-size: 15px;
    font-weight: 500;
    color: var(--text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.friend-status {
    font-size: 11px;
    color: var(--text-color-placeholder);
    padding: 2px 6px;
    background: var(--border-color-light);
    border-radius: var(--border-radius-round);
}

.friend-status.online {
    color: var(--color-success);
    background: #ecfdf5;
}

.friend-last-message {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.last-message {
    font-size: 13px;
    color: var(--text-color-secondary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 140px;
}

.last-message-time {
    font-size: 11px;
    color: var(--text-color-placeholder);
}
</style>