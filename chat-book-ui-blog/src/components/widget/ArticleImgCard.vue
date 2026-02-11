<template>
    <div class="post-card">
        <div class="post-picture" @click='openArticle(post.id)'>
            <el-image :src="post.cover" fit="cover" style="width: 150px;height: 100px" />
        </div>
        <div class="post-context">
            <div class="post-header" @click="openArticle(post.id)">
                <h4 class="post-title">{{ post.title }}</h4>
            </div>
            <div class="post-summary" @click="openArticle(post.id)">
                <el-text truncated>{{ post.abstractText }}</el-text>
            </div>
            <div class="post-footer">
                <div class="post-meta" @click="openArticle(post.id)">
                    <el-avatar class="userInfo" :src="post.authorAvatar" size="small" style="margin-right: 10px" />
                    <div class="meta-details">
                        <el-text class="userInfo"><b>{{ post.userName }}</b></el-text>
                        <el-text class="userInfo">{{ post.createTime }}</el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <View />
                            </el-icon>
                            {{ post.viewCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <ChatDotSquare />
                            </el-icon>
                            {{ post.commentCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <Star />
                            </el-icon>
                            {{ post.collectCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <Pointer />
                            </el-icon>
                            {{ post.praiseCount }}
                        </el-text>
                    </div>
                </div>
                <div class="post-controls">
                    <button class="controls-txt" @click="handleEdit(post.id)">编辑</button>
                    <button class="controls-txt">删除</button>
                </div>
            </div>
        </div>
    </div>
</template>


<script setup>
import { ChatDotSquare, Pointer, Star, View } from "@element-plus/icons-vue";
import { ElAvatar } from "element-plus";
import router from "@/router/index.js";

defineProps({
    post: {
        type: Object,
        required: true
    }
})
const openArticle = async (id) => {
    console.log("文章id->", id)
    await router.push({ name: 'Article', params: { id } })
}
const handleEdit = async (id) => {
    await router.push({ name: 'Edit', params: { id } })
}
</script>

<style scoped>
.post-card {
    display: flex;
    padding: 24px;
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.5);
    border-radius: 16px;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
    transition: all 0.3s ease;
    margin-bottom: 16px;
}

.post-card:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 30px rgba(0, 0, 0, 0.1);
    background: rgba(255, 255, 255, 0.9);
}

.post-picture {
    width: 180px;
    height: 120px;
    margin-right: 24px;
    border-radius: 12px;
    overflow: hidden;
    flex-shrink: 0;
}

.post-picture :deep(.el-image) {
    width: 100% !important;
    height: 100% !important;
    transition: transform 0.3s ease;
}

.post-picture:hover :deep(.el-image) {
    transform: scale(1.05);
}

.post-context {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    min-width: 0;
    /* Fix flex child overflow */
}

.post-header {
    margin-bottom: 8px;
    cursor: pointer;
}

.post-title {
    font-size: 18px;
    font-weight: 600;
    color: #1f2937;
    margin: 0;
    line-height: 1.4;
    transition: color 0.2s;
}

.post-title:hover {
    color: #3b82f6;
}

.post-summary {
    margin-bottom: 16px;
    width: 100%;
}

.post-summary :deep(.el-text) {
    font-size: 14px;
    color: #6b7280;
    line-height: 1.6;
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
}

.meta-details {
    display: flex;
    align-items: center;
    gap: 16px;
}

.userInfo {
    display: flex;
    align-items: center;
    font-size: 13px;
    color: #9ca3af;
    gap: 4px;
}

.userInfo b {
    color: #4b5563;
    font-weight: 500;
}

.el-icon-img {
    font-size: 14px;
    margin-right: 2px;
}

.post-controls {
    display: flex;
    gap: 8px;
}

.controls-txt {
    background: rgba(59, 130, 246, 0.1);
    padding: 6px 16px;
    border-radius: 20px;
    font-size: 12px;
    color: #3b82f6;
    border: 1px solid transparent;
    cursor: pointer;
    transition: all 0.2s;
    font-weight: 500;
}

.controls-txt:hover {
    background: #3b82f6;
    color: white;
    box-shadow: 0 2px 8px rgba(59, 130, 246, 0.3);
}

.controls-txt:last-child {
    background: rgba(239, 68, 68, 0.1);
    color: #ef4444;
}

.controls-txt:last-child:hover {
    background: #ef4444;
    color: white;
    box-shadow: 0 2px 8px rgba(239, 68, 68, 0.3);
}
</style>