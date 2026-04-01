<template>
    <div class="notification-message-card">
        <div class="notification-avatar-wrap">
            <!-- NotificationVO 暂未包含头像，使用默认图片 -->
            <el-avatar class="user-avatar" :size="40" :src="null" />
        </div>
        <div class="notification-content">
            <div class="notification-header">
                <div class="notification-user-action">
                    <span class="notification-username">用户&nbsp;{{ message.senderId }}</span>
                    <span class="notification-action-text">{{ actionText }}</span>
                    <span class="notification-article-link" @click.stop>
                        《{{ message.articleTitle || '未知文章' }}》
                    </span>
                </div>
                <span class="notification-time">{{ message.createTime }}</span>
            </div>
        </div>
        <div class="notification-action-icon">
            <el-tag :type="tagType" size="small" effect="light" class="notification-type-tag">
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

// P0 Fix: 直接读取后端返回的 actionType 字段（PRAISE/COLLECT/COMMENT/BROWSE）
// 原来通过 collectCount/praiseCount 数量猜测类型的逻辑已废弃
const ACTION_TYPE_MAP = {
    COLLECT: 'collect',
    PRAISE:  'like',
    COMMENT: 'comment',
    BROWSE:  'view',
};

const messageType = computed(() => {
    return ACTION_TYPE_MAP[props.message.actionType] || 'view';
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
