<template>
    <div class="article-card-inner" :class="[`variant-${variant}`]">
        <!-- 纯文本几何纹理背景 -->
        <div v-if="variant === 'text-only'" class="card-geometric-bg"></div>

        <!-- 图片展示层 -->
        <div v-if="showCover" class="card-cover-wrapper" :class="{ 'ratio-80': variant === 'large-image' }">
            <el-image :src="post.cover || defaultCover" lazy fit="cover" class="cover-image">
                <template #placeholder>
                    <el-skeleton-item variant="image" class="w-full h-full skeleton-img"
                        style="width: 100%; height: 100%; position: absolute; inset: 0;" />
                </template>
            </el-image>
            <div class="card-overlay" v-if="['feature', 'large-image'].includes(variant)"></div>
            <div class="category-badge" v-if="variant === 'feature'">{{ getCategoryName(post.category) }}</div>
        </div>

        <!-- 文本内容层 -->
        <div class="card-content">
            <div class="post-header">
                <h4 class="post-title" :class="{ 'title-large': variant === 'feature' }" :title="post.title">{{
                    post.title }}
                </h4>
            </div>

            <div class="post-summary" v-if="variant !== 'large-image'">
                <p class="summary-text">{{ post.abstractText || '探索更深入的技术细节，点击阅读全文...' }}</p>
            </div>

            <!-- 大尺寸卡片的特殊占位 -->
            <div style="flex: 1" v-if="variant === 'large-image'"></div>

            <div class="post-footer" :class="{ 'footer-glow': variant === 'large-image' }">
                <div class="user-info">
                    <el-avatar class="user-avatar" :src="post.authorAvatar || defaultAvatar" :size="20" />
                    <span class="author-name">{{ post.userName }}</span>
                </div>
                <div class="meta-stats">
                    <!-- Feature and Large Image hide time for clean style -->
                    <span class="meta-item time" v-if="!['large-image', 'feature'].includes(variant)">{{
                        post.createTime?.substring(0, 10) }}</span>
                    <span class="meta-item"><el-icon>
                            <View />
                        </el-icon> {{ post.viewCount || 0 }}</span>
                    <span class="meta-item"><el-icon>
                            <Star />
                        </el-icon> {{ post.praiseCount || 0 }}</span>
                </div>
            </div>

            <div class="post-category" v-if="variant !== 'feature' && variant !== 'large-image'">
                <span class="category-tag">{{ getCategoryName(post.category) }}</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ChatDotSquare, Star, View } from "@element-plus/icons-vue";
import { ElAvatar, ElImage, ElSkeletonItem, ElIcon } from "element-plus";
import { getCategoryName } from "@/utils/category.js";
import { computed } from 'vue';

const props = defineProps({
    post: {
        type: Object,
        required: true
    },
    variant: {
        type: String,
        default: 'default' // 'feature', 'large-image', 'text-only', 'image', 'default'
    }
});

const defaultCover = 'https://img.shetu66.com/2023/06/26/1687770031227597.png';
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec64369c2642b92c6726f1epng.png';

const showCover = computed(() => {
    return ['feature', 'large-image', 'image'].includes(props.variant);
});
</script>

<style scoped>
.article-card-inner {
    position: relative;
    display: flex;
    flex-direction: column;
    height: 100%;
}

.variant-text-only {
    padding: 24px;
    background: transparent;
    overflow: hidden;
}

.card-geometric-bg {
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    opacity: 0.15;
    background-image:
        radial-gradient(var(--color-primary-active) 1px, transparent 1px),
        radial-gradient(var(--color-primary-active) 1px, transparent 1px);
    background-size: 24px 24px;
    background-position: 0 0, 12px 12px;
    z-index: 0;
    pointer-events: none;
}

.variant-feature,
.variant-image,
.variant-large-image,
.variant-default {
    padding: 0;
    background: transparent;
}

.card-cover-wrapper {
    position: relative;
    width: 100%;
    aspect-ratio: 16 / 9;
    overflow: hidden;
}

/* Feature 占满卡片，更具张力 */
.variant-feature .card-cover-wrapper {
    aspect-ratio: 21 / 9;
}

/* 大型卡片重构 */
.card-cover-wrapper.ratio-80 {
    position: absolute;
    /* 使图片变成背景层 */
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    aspect-ratio: auto;
    height: 100%;
    z-index: 0;
}

.cover-image {
    width: 100%;
    height: 100%;
    transition: transform 0.6s cubic-bezier(0.25, 0.46, 0.45, 0.94);
}

.article-card-inner:hover .cover-image {
    transform: scale(1.05);
}

.card-overlay {
    position: absolute;
    inset: 0;
}

/* 渐变遮罩 */
.variant-large-image .card-overlay {
    background: linear-gradient(to top, rgba(0, 0, 0, 0.85) 0%, rgba(0, 0, 0, 0.3) 50%, rgba(0, 0, 0, 0.1) 100%);
    pointer-events: none;
}

.variant-feature .card-overlay {
    background: linear-gradient(to top, rgba(var(--color-primary-rgb), 0.1) 0%, transparent 100%);
    mix-blend-mode: multiply;
    pointer-events: none;
}

.category-badge {
    position: absolute;
    top: 16px;
    left: 16px;
    background: rgba(255, 255, 255, 0.85);
    backdrop-filter: blur(8px);
    color: var(--color-primary);
    padding: 6px 14px;
    border-radius: 20px;
    font-size: 0.75rem;
    font-weight: 700;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.card-content {
    padding: 20px 24px;
    flex: 1;
    display: flex;
    flex-direction: column;
    position: relative;
    z-index: 1;
}

/* Large image 文字浮动 */
.variant-large-image .card-content {
    justify-content: flex-end;
    background: transparent;
    min-height: 280px;
}

.variant-large-image .post-title,
.variant-large-image .summary-text,
.variant-large-image .author-name,
.variant-large-image .meta-item {
    color: rgba(255, 255, 255, 0.95);
}

.post-header {
    margin-bottom: 12px;
}

.post-title {
    font-size: 1.125rem;
    font-weight: 700;
    color: var(--text-color-primary);
    line-height: 1.5;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
    transition: color 0.3s;
}

.variant-large-image .post-title {
    -webkit-line-clamp: 3;
    line-clamp: 3;
    font-size: 1.35rem;
}

.title-large {
    font-size: 1.6rem;
    font-weight: 800;
    -webkit-line-clamp: 2;
    line-clamp: 2;
}

.article-card-inner:hover .post-title {
    color: var(--color-primary);
}

.variant-large-image:hover .post-title {
    color: #fff;
}

.post-summary {
    margin-bottom: 16px;
    flex: 1;
}

.summary-text {
    font-size: 0.875rem;
    color: var(--text-color-regular);
    line-height: 1.6;
    margin: 0;
    display: -webkit-box;
    -webkit-line-clamp: 3;
    line-clamp: 3;
    -webkit-box-orient: vertical;
    overflow: hidden;
}

.post-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
}

.user-info {
    display: flex;
    align-items: center;
    gap: 8px;
}

.author-name {
    font-size: 0.8125rem;
    font-weight: 600;
    color: var(--text-color-regular);
}

.meta-stats {
    display: flex;
    align-items: center;
    gap: 12px;
    font-size: 0.8125rem;
    color: var(--text-color-secondary);
}

.meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
}

.post-category {
    display: flex;
    align-items: flex-start;
}

.category-tag {
    background-color: var(--color-primary-light);
    color: var(--color-primary);
    padding: 4px 10px;
    border-radius: 6px;
    font-size: 0.75rem;
    font-weight: 600;
    transition: all 0.2s;
}

.article-card-inner:hover .category-tag {
    background-color: var(--color-primary);
    color: white;
}
</style>