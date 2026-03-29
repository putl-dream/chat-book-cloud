<template>
    <div class="post-card c-article-card-shell">
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
                <div class="post-controls c-article-card-actions">
                    <button class="controls-txt c-article-card-action" @click="handleEdit(post.id)">编辑</button>
                    <button class="controls-txt c-article-card-action c-article-card-action--danger">删除</button>
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
