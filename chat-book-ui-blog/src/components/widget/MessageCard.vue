<template>
    <div class="message-card">
        <div class="avatar-wrapper">
            <el-avatar class="user-avatar" :size="40" :src="message.authorAvatar"></el-avatar>
        </div>
        <div class="message-content">
            <div class="message-header">
                <div class="user-action">
                    <span class="username">{{ message.author }}</span>
                    <span class="action-text">{{ actionText }}</span>
                    <span class="article-link" @click.stop>
                        《{{ message.title }}》
                    </span>
                </div>
                <span class="time">{{ message.createTime }}</span>
            </div>
            
            <div class="message-body" v-if="message.summary">
                <div class="quote-box">
                    {{ message.summary }}
                </div>
            </div>
        </div>
        <div class="action-icon">
            <el-tag :type="tagType" size="small" effect="light" class="type-tag">
                {{ tagText }}
            </el-tag>
        </div>
    </div>
</template>

<script setup>
import { computed } from 'vue';

const props = defineProps({
    message: {
        type: Object,
        required: true
    }
});

const messageType = computed(() => {
    if (props.message.collectCount === 1) return 'collect';
    if (props.message.praiseCount === 1) return 'like';
    if (props.message.reviewCount === 1) return 'comment';
    return 'view';
});

const actionText = computed(() => {
    const map = {
        collect: '收藏了你的文章',
        like: '点赞了你的文章',
        comment: '评论了你的文章',
        view: '浏览了你的文章'
    };
    return map[messageType.value];
});

const tagType = computed(() => {
    const map = {
        collect: 'warning',
        like: 'danger',
        comment: 'primary',
        view: 'info'
    };
    return map[messageType.value];
});

const tagText = computed(() => {
    const map = {
        collect: '收藏',
        like: '点赞',
        comment: '评论',
        view: '浏览'
    };
    return map[messageType.value];
});
</script>

<style scoped>
.message-card {
    display: flex;
    gap: 16px;
    padding: 16px;
    background: var(--bg-color-white);
    transition: background-color 0.2s;
}

.message-card:hover {
    background-color: var(--bg-color-base);
}

.avatar-wrapper {
    flex-shrink: 0;
}

.message-content {
    flex: 1;
    min-width: 0;
}

.message-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
}

.user-action {
    font-size: 14px;
    line-height: 1.5;
}

.username {
    font-weight: 600;
    color: var(--text-color-primary);
    margin-right: 6px;
}

.action-text {
    color: var(--text-color-regular);
    margin-right: 6px;
}

.article-link {
    color: var(--color-primary);
    cursor: pointer;
    font-weight: 500;
}

.article-link:hover {
    text-decoration: underline;
}

.time {
    font-size: 12px;
    color: var(--text-color-secondary);
    white-space: nowrap;
    margin-left: 12px;
}

.quote-box {
    background-color: var(--bg-color-base);
    padding: 10px 12px;
    border-radius: 4px;
    font-size: 14px;
    color: var(--text-color-regular);
    line-height: 1.6;
    margin-top: 4px;
}

.action-icon {
    flex-shrink: 0;
    align-self: flex-start;
}

.type-tag {
    font-weight: 500;
}
</style>
