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
