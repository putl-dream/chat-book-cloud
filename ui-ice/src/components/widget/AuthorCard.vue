<template>
    <div class="author-card-container">
        <h3 class="card-title">作者介绍</h3>
        <div class="author-info">
            <div class="avatar-wrapper">
                <el-avatar :size="80" :src="userInfo.photo" alt="Author Avatar"></el-avatar>
            </div>
            <div class="author-details">
                <p class="author-name">{{ userInfo.username || '作者' }}</p>
                <p class="join-date">已加入 707 天</p>
                <div class="action-buttons">
                    <el-button type="primary" class="follow-btn">关注作者</el-button>
                    <el-button class="manual-btn">查看教程</el-button>
                </div>
            </div>
        </div>
        <div class="stats-grid">
            <div class="stats-item">
                <span class="stats-label">文章</span>
                <span class="stats-value">28</span>
            </div>
            <div class="stats-item">
                <span class="stats-label">点赞</span>
                <span class="stats-value">1.2k</span>
            </div>
            <div class="stats-item">
                <span class="stats-label">收藏</span>
                <span class="stats-value">856</span>
            </div>
            <div class="stats-item">
                <span class="stats-label">粉丝</span>
                <span class="stats-value">432</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref, watch} from "vue";
import {getUserById} from "@/api/user.js";

const props = defineProps({
    userId: {
        required: true,
    }
});
const userInfo = ref({});

const queryUserInfo = async () => {
    if (props.userId) {
        const param = await getUserById(props.userId);
        if (param.code === 200) {
            userInfo.value = param.data;
        }
    }
};

onMounted(queryUserInfo);
watch(() => props.userId, queryUserInfo);
</script>

<style scoped>
.author-card-container {
    padding: 24px;
    background: var(--bg-color-white);
}

.card-title {
    font-size: 1rem;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 24px;
    display: flex;
    align-items: center;
    gap: 8px;
}

.card-title::before {
    content: '';
    width: 4px;
    height: 16px;
    background: var(--color-primary);
    border-radius: var(--border-radius-round);
}

.author-info {
    text-align: center;
    padding-bottom: 24px;
    border-bottom: 1px solid var(--border-color-lighter);
}

.avatar-wrapper {
    margin-bottom: 16px;
    display: inline-block;
    padding: 4px;
    border-radius: 50%;
    background: linear-gradient(135deg, var(--color-primary-light), #e0e7ff);
}

.author-name {
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--text-color-primary);
    margin: 0 0 8px;
}

.join-date {
    font-size: 0.8125rem;
    color: var(--text-color-secondary);
    margin: 0 0 20px;
}

.action-buttons {
    display: flex;
    gap: 12px;
    justify-content: center;
}

.action-buttons .el-button {
    border-radius: var(--border-radius-large);
    padding: 10px 20px;
    font-weight: 600;
    font-size: 0.8125rem;
    transition: var(--transition-base);
}

.follow-btn {
    border: none;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.2);
}

.manual-btn {
    color: var(--color-primary);
    border-color: var(--color-primary-light);
    background: var(--color-primary-light);
}

.manual-btn:hover {
    background: var(--color-primary);
    color: white;
}

.stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 12px;
    padding-top: 24px;
}

.stats-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 4px;
}

.stats-label {
    font-size: 0.75rem;
    color: var(--text-color-secondary);
}

.stats-value {
    font-size: 0.9375rem;
    font-weight: 700;
    color: var(--text-color-primary);
}
</style>
