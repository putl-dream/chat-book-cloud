<template>
    <header class="article-header">
        <h1 class="article-title">{{ article.title }}</h1>

        <div class="article-header-meta">
            <div class="author-strip">
                <el-avatar :size="42" :src="article.authorAvatar" class="author-avatar">
                    {{ (article.userName || '作').slice(0, 1) }}
                </el-avatar>
                <div class="author-copy">
                    <span class="author-kicker">Author</span>
                    <span class="author-name">{{ article.userName || '作者' }}</span>
                </div>
            </div>

            <div class="meta-cluster">
                <button type="button" class="meta-panel-btn"
                    :class="{ 'is-active': activePanel === PANEL_TYPE.DEFAULT && showRightPanel }"
                    @click="$emit('openDefaultPanel')">
                    目录
                </button>
                <div class="article-meta">
                    <div class="meta-item">
                        <span class="label">发布于</span>
                        <span class="value">{{ article.createTime }}</span>
                    </div>
                    <div class="meta-divider"></div>
                    <div class="meta-item">
                        <span class="label">阅读</span>
                        <span class="value">{{ article.viewCount }}</span>
                    </div>
                </div>
            </div>
        </div>
    </header>
</template>

<script setup>
import { PANEL_TYPE } from "../_utils/config.js";

const props = defineProps({
    article: { type: Object, default: () => ({}) },
    activePanel: { type: String, default: '' },
    showRightPanel: { type: Boolean, default: false }
});

const emit = defineEmits(['openDefaultPanel']);
</script>
