<template>
    <aside
        class="sidebar desktop-only"
        :class="{ 'is-collapsed': isSidebarCollapsed, 'is-expanded': isSidebarExpanded }"
        @mouseenter="handleSidebarHover(true)"
        @mouseleave="handleSidebarHover(false)"
        @click="handleSidebarClick">
        <div class="sidebar-inner">
            <div class="article-buttons">
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': praiseStat !== 0 }" @click.stop="$emit('handleLike')">
                        <el-icon><Pointer /></el-icon>
                    </el-button>
                    <span class="action-label" title="点赞">点赞</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': collectStat !== 0 }" @click.stop="$emit('handleFavorite')">
                        <el-icon><Star /></el-icon>
                    </el-button>
                    <span class="action-label" title="收藏">收藏</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.COMMENT && showRightPanel }" @click.stop="$emit('handleComment')">
                        <el-icon><ChatLineRound /></el-icon>
                    </el-button>
                    <span class="action-label" title="评论">评论</span>
                </div>

                <div class="action-divider" aria-hidden="true"></div>

                <div v-if="!isSelfAuthor" class="action-item">
                    <el-button
                        class="action-btn"
                        size="large"
                        circle
                        :loading="authorActionLoading"
                        :class="{ 'is-active': isFollowing }"
                        @click.stop="$emit('handleFollow')">
                        <el-icon><UserFilled /></el-icon>
                    </el-button>
                    <span class="action-label" :title="followLabel">{{ followLabel }}</span>
                </div>
                <div class="action-item">
                    <el-button class="action-btn" size="large" circle :class="{ 'is-active': activePanel === PANEL_TYPE.AI && showRightPanel }" @click.stop="$emit('handleAiChat')">
                        <el-icon><Service /></el-icon>
                    </el-button>
                    <span class="action-label" title="AI 助手">AI</span>
                </div>
            </div>
        </div>
    </aside>
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
    followLabel: { type: String, default: '关注' },
    isSidebarCollapsed: { type: Boolean, default: false },
    isSidebarExpanded: { type: Boolean, default: false }
});

const emit = defineEmits([
    'handleLike', 
    'handleFavorite', 
    'handleComment', 
    'handleFollow', 
    'handleAiChat',
    'handleSidebarHover',
    'handleSidebarClick'
]);

const handleSidebarHover = (isHovering) => {
    emit('handleSidebarHover', isHovering);
};

const handleSidebarClick = () => {
    emit('handleSidebarClick');
};
</script>

<style scoped>
.sidebar {
    position: sticky;
    top: 60px;
    height: calc(100vh - 60px);
    transition: width 0.3s ease, transform 0.3s ease, background-color 0.3s ease;
    display: flex;
    flex-direction: column;
    justify-content: center;
    box-sizing: border-box;
    z-index: 20;
    overflow: hidden;
}

.sidebar-inner {
    width: 70%;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
    align-items: center;
    transition: width 0.3s ease;
}

.article-buttons {
    display: flex;
    flex-direction: column;
    gap: 24px;
    width: 100%;
    align-items: center;
}

.action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 6px;
    width: 100%;
    max-width: 100%;
    overflow: hidden;
}

.action-divider {
    width: 30px;
    height: 1px;
    background: linear-gradient(90deg, rgba(15, 23, 42, 0), rgba(15, 23, 42, 0.22), rgba(15, 23, 42, 0));
}

.action-btn {
    width: 40px;
    height: 40px;
    border: 1px solid var(--border-color-base);
    background: rgba(255, 255, 255, 0.92);
    box-shadow: none;
    transition: color 0.2s, background 0.2s, border-color 0.2s, transform 0.2s;
    font-size: 18px;
    color: var(--text-color-secondary);
    flex-shrink: 0;
}

.action-btn:hover {
    color: var(--color-primary);
    border-color: var(--color-primary);
    transform: translateY(-1px);
    background: rgba(255, 255, 255, 1);
}

.action-btn.is-active {
    background: var(--color-primary);
    color: white;
    border-color: var(--color-primary);
}

.action-label {
    font-size: 11px;
    color: var(--text-color-secondary);
    opacity: 0.85;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    max-width: 100%;
    text-align: center;
}

.desktop-only {
    display: flex;
}

@media (max-width: 1024px) {
    .sidebar {
        position: fixed;
        left: 0;
        top: 0;
        width: 60px;
        background-color: rgba(255, 255, 255, 0.8);
        backdrop-filter: blur(8px);
        border-right: 1px solid rgba(0, 0, 0, 0.05);
        box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
        cursor: pointer;
    }

    .sidebar.is-collapsed .sidebar-inner {
        width: 100%;
    }

    .sidebar.is-expanded {
        width: 70%;
        background-color: #fff;
        box-shadow: 4px 0 16px rgba(0, 0, 0, 0.1);
        cursor: default;
    }

    .sidebar.is-expanded .sidebar-inner {
        width: 70%;
    }
}

@media (max-width: 768px) {
    .desktop-only {
        display: none !important;
    }
}
</style>
