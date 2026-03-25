<template>
    <div class="mobile-toolbar mobile-only">
        <div class="action-item">
            <el-button class="action-btn" size="large" circle :class="{ 'is-active': praiseStat !== 0 }" @click.stop="$emit('handleLike')">
                <el-icon><Pointer /></el-icon>
            </el-button>
            <span class="action-label">点赞</span>
        </div>
        <div class="action-item">
            <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }" @click.stop="$emit('handleFavorite')">
                <el-icon><Star /></el-icon>
            </el-button>
            <span class="action-label">收藏</span>
        </div>
        <div class="action-item">
            <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.COMMENT && showRightPanel }" @click.stop="$emit('handleComment')">
                <el-icon><ChatLineRound /></el-icon>
            </el-button>
            <span class="action-label">评论</span>
        </div>
        <div v-if="!isSelfAuthor" class="action-item">
            <el-button class="action-btn" size="large" circle :loading="authorActionLoading" :class="{ 'is-active': isFollowing }" @click.stop="$emit('handleFollow')">
                <el-icon><UserFilled /></el-icon>
            </el-button>
            <span class="action-label">{{ followLabel }}</span>
        </div>
        <div class="action-item">
            <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.AI && showRightPanel }" @click.stop="$emit('handleAiChat')">
                <el-icon><Service /></el-icon>
            </el-button>
            <span class="action-label">AI</span>
        </div>
    </div>
</template>

<script setup>
import { ChatLineRound, Pointer, Service, Star, UserFilled } from '@element-plus/icons-vue';
import { PANEL_TYPE } from "../_utils/config.js";

const props = defineProps({
    praiseStat: { type: Number, default: 0 },
    collectStat: { type: Number, default: 0 },
    activePanel: { type: String, default: '' },
    showRightPanel: { type: Boolean, default: false },
    isSelfAuthor: { type: Boolean, default: false },
    authorActionLoading: { type: Boolean, default: false },
    isFollowing: { type: Boolean, default: false },
    followLabel: { type: String, default: '关注' }
});

const emit = defineEmits([
    'handleLike', 
    'handleFavorite', 
    'handleComment', 
    'handleFollow', 
    'handleAiChat'
]);
</script>

<style scoped>
.mobile-only {
    display: none !important;
}

@media (max-width: 768px) {
    .mobile-only {
        display: flex !important;
    }

    .mobile-toolbar {
        position: fixed;
        bottom: 0;
        left: 0;
        right: 0;
        height: 60px;
        background: rgba(255, 255, 255, 0.95);
        backdrop-filter: blur(10px);
        -webkit-backdrop-filter: blur(10px);
        justify-content: space-around;
        align-items: center;
        z-index: 100;
        border-top: 1px solid rgba(0, 0, 0, 0.1);
        padding: 0 8px;
    }

    .mobile-toolbar .action-item {
        flex: 1;
        max-width: 70px;
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 4px;
    }

    .mobile-toolbar .action-btn {
        width: 36px;
        height: 36px;
        font-size: 16px;
        border: 1px solid var(--border-color-base);
        background: rgba(255, 255, 255, 0.92);
        box-shadow: none;
        transition: color 0.2s, background 0.2s, border-color 0.2s, transform 0.2s;
        color: var(--text-color-secondary);
        flex-shrink: 0;
    }
    
    .mobile-toolbar .action-btn.is-active {
        background: var(--color-primary);
        color: white;
        border-color: var(--color-primary);
    }

    .mobile-toolbar .action-label {
        font-size: 10px;
        color: var(--text-color-secondary);
        opacity: 0.85;
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
        max-width: 100%;
        text-align: center;
    }
}
</style>
