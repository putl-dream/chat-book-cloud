<template>
    <div class="user-card-widget">
        <div class="avatar-area">
            <el-avatar :size="70" :src="user.photo" class="avatar"></el-avatar>
        </div>
        <div class="user-details">
            <div class="name-row">
                <span class="username">{{ user.username }}</span>
                <el-tag size="small" type="info" effect="plain" class="join-tag">已加入60天</el-tag>
            </div>
            <p class="bio">{{ user.profile || '暂无介绍' }}</p>
        </div>
        
        <div class="stats-grid">
            <div class="stat-item">
                <span class="label">文章</span>
                <span class="value">28</span>
            </div>
            <div class="stat-item">
                <span class="label">点赞</span>
                <span class="value">28</span>
            </div>
            <div class="stat-item">
                <span class="label">收藏</span>
                <span class="value">28</span>
            </div>
            <div class="stat-item">
                <span class="label">粉丝</span>
                <span class="value">28</span>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    user: {
        type: Object,
        required: true
    }
})
</script>

<style scoped>
.user-card-widget {
    padding: 16px;
    background: var(--bg-color-white);
}

.avatar-area {
    display: flex;
    justify-content: center;
    margin-bottom: 12px;
}

.user-details {
    text-align: center;
    margin-bottom: 16px;
    padding-bottom: 16px;
    border-bottom: 1px solid var(--border-color-lighter);
}

.name-row {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 8px;
}

.username {
    font-size: 16px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.bio {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin: 0;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 8px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.label {
    font-size: 12px;
    color: var(--text-color-secondary);
}

.value {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary);
}
</style>