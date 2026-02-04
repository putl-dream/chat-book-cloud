<template>
    <div class="article-card">
        <div class="post-header">
            <h4 class="post-title">{{ post.title }}</h4>
        </div>
        <div class="post-summary">
            <p class="summary-text">{{ post.summary }}</p>
        </div>
        <div class="post-footer">
            <div class="post-meta">
                <div class="author-info">
                    <el-avatar class="user-avatar" :src="post.authorAvatar" :size="24"/>
                    <span class="author-name">{{ post.author }}</span>
                </div>
                
                <div class="meta-stats">
                    <span class="meta-item time">{{ post.createTime }}</span>
                    <span class="meta-item">
                        <el-icon><View/></el-icon> {{ post.viewCount }}
                    </span>
                    <span class="meta-item">
                        <el-icon><ChatDotSquare/></el-icon> {{ post.commentCount }}
                    </span>
                    <span class="meta-item">
                        <el-icon><Star/></el-icon> {{ post.praiseCount }}
                    </span>
                </div>
            </div>
            <div class="post-category">
                <span class="category-tag">{{ getCategoryName(post.category) }}</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import {ChatDotSquare, Star, View} from "@element-plus/icons-vue";
import {ElAvatar} from "element-plus";
import {getCategoryName} from "@/utils/category.js";

defineProps({
    post: {
        type: Object,
        required: true
    }
});
</script>

<style scoped>
.article-card {
    padding: 12px 16px;
    transition: var(--transition-base);
    border-radius: var(--border-radius-large);
}

.article-card:hover {
    background: var(--bg-color-light);
}

.post-header {
    margin-bottom: 12px;
}

.post-title {
    font-size: 1.15rem;
    font-weight: 600;
    color: var(--text-color-primary);
    line-height: 1.5;
    margin: 0;
    transition: var(--transition-fast);
}

.article-card:hover .post-title {
    color: var(--color-primary);
}

.post-summary {
    margin-bottom: 16px;
}

.summary-text {
    font-size: 0.875rem;
    color: var(--text-color-regular);
    line-height: 1.6;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
}

.post-meta {
    display: flex;
    align-items: center;
    gap: 20px;
    color: var(--text-color-secondary);
}

.author-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.author-name {
    font-size: 0.8125rem;
    font-weight: 500;
    color: var(--text-color-regular);
}

.meta-stats {
    display: flex;
    align-items: center;
    gap: 16px;
    font-size: 0.8125rem;
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.meta-item .el-icon {
    font-size: 14px;
}

.post-category {
    display: flex;
    align-items: center;
}

.category-tag {
    background-color: var(--color-primary-light);
    color: var(--color-primary);
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: 500;
    transition: var(--transition-fast);
}

.category-tag:hover {
    background-color: var(--color-primary);
    color: white;
}
</style>