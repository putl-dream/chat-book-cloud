<template>
    <div class="article-card-inner c-article-card-shell"
        :class="[`variant-${variant}`, symmetryClass, { 'c-article-card-shell--plain': ['feature', 'large-image', 'bento-secondary'].includes(variant) }]">
        <!-- 纯文本几何纹理背景 -->
        <div v-if="variant === 'text-only'" class="card-geometric-bg"></div>

        <div v-if="showCover" class="card-cover-wrapper" :class="{ 'ratio-80': ['large-image', 'image'].includes(variant) }">
            <el-image :src="finalCover" lazy fit="cover" class="cover-image">
                <template #placeholder>
                    <el-skeleton-item variant="image" class="w-full h-full skeleton-img"
                        style="width: 100%; height: 100%; position: absolute; inset: 0;" />
                </template>
            </el-image>
            <div class="card-overlay" v-if="['feature', 'large-image', 'image'].includes(variant)"></div>
            <div class="category-badge" v-if="variant === 'feature'">{{ getCategoryName(post.category) }}</div>
        </div>

        <!-- 文本内容?-->
        <div class="card-content">
            <div class="post-header">
                <h4 class="post-title" :class="{ 'title-large': variant === 'feature', 'title-small': ['image', 'bento-secondary'].includes(variant) }" :title="post.title">{{
                    post.title }}
                </h4>
            </div>

            <div class="post-summary" v-if="!['large-image', 'image', 'bento-secondary'].includes(variant)">
                <p class="summary-text">{{ post.abstractText || '探索更深入的技术细节，点击阅读全文...' }}</p>
            </div>

            <!-- 大尺寸卡片的特殊占位 -->
            <div style="flex: 1" v-if="['large-image', 'image'].includes(variant)"></div>

            <div class="post-footer" :class="{ 'footer-glow': ['large-image', 'image'].includes(variant) }">
                <div class="user-info">
                    <el-avatar class="user-avatar" :src="post.authorAvatar || defaultAvatar" :size="20" />
                    <span class="author-name">{{ post.userName }}</span>
                </div>
                <div class="meta-stats">
                    <!-- Feature, Large Image, Image, and Bento Secondary hide time for clean style -->
                    <span class="meta-item time" v-if="!['large-image', 'feature', 'image', 'bento-secondary'].includes(variant)">{{
                        post.createTime?.substring(0, 10) }}</span>
                    <span class="meta-item"><el-icon>
                            <View />
                        </el-icon> {{ post.viewCount || 0 }}</span>
                    <span class="meta-item"><el-icon>
                            <Star />
                        </el-icon> {{ post.praiseCount || 0 }}</span>
                </div>
            </div>

            <!-- <div class="post-category" v-if="variant !== 'feature' && variant !== 'large-image'">
                <span class="category-tag">{{ getCategoryName(post.category) }}</span>
            </div> -->
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

const finalCover = computed(() => {
    if (props.post.cover) return props.post.cover;
    // 使用文章ID作为随机种子，生成稳定且高质量的默认封面，保证没有封面的文章在瀑布流中依然有吸引力
    const seed = props.post.id || props.post.title || Math.floor(Math.random() * 1000);
    return `https://picsum.photos/seed/${seed}/800/600`;
});

const symmetryClass = computed(() => {
    // 仅针对普通卡片应用非对称排列样式，专属模式保持独立排版
    if (['feature', 'large-image', 'text-only', 'bento-secondary', 'image'].includes(props.variant)) return '';

    // 基于ID或标题生成确定的随机数，保证翻页时卡片样式稳定
    let seed = 0;
    if (props.post.id) {
        seed = typeof props.post.id === 'string' ? props.post.id.charCodeAt(0) : props.post.id;
    } else if (props.post.title) {
        seed = props.post.title.charCodeAt(0);
    } else {
        seed = Math.floor(Math.random() * 1000);
    }

    const pattern = (seed % 4) + 1; // 1, 2, 3, 4
    return `asymmetric-style-${pattern}`;
});

const showCover = computed(() => {
    return ['feature', 'large-image', 'image', 'bento-secondary', 'default'].includes(props.variant);
});
</script>
