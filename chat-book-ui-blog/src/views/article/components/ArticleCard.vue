<template>
    <div
        class="article-card-inner c-article-card-shell"
        :class="[`variant-${variant}`, { 'c-article-card-shell--plain': ['feature', 'image', 'large-image'].includes(variant) }]">
        <!-- 纯文本几何纹理背?-->
        <div v-if="variant === 'text-only'" class="card-geometric-bg"></div>

        <!-- 图片展示?-->
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

        <!-- 文本内容?-->
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
