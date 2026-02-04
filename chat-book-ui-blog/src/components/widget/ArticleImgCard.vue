<template>
    <div class="post-card">
        <div class="post-picture" @click='openArticle(post.id)'>
            <el-image :src="post.articleCover" fit="cover" style="width: 150px;height: 100px"/>
        </div>
        <div class="post-context">
            <div class="post-header" @click="openArticle(post.id)">
                <h4 class="post-title">{{ post.title }}</h4>
            </div>
            <div class="post-summary" @click="openArticle(post.id)">
                <el-text truncated>{{ post.summary }}</el-text>
            </div>
            <div class="post-footer">
                <div class="post-meta" @click="openArticle(post.id)">
                    <el-avatar class="userInfo" :src="post.authorAvatar" size="small" style="margin-right: 10px"/>
                    <div class="meta-details">
                        <el-text class="userInfo"><b>{{ post.author }}</b></el-text>
                        <el-text class="userInfo">{{ post.time }}</el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <View/>
                            </el-icon>
                            {{ post.viewCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <ChatDotSquare/>
                            </el-icon>
                            {{ post.commentCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <Star/>
                            </el-icon>
                            {{ post.collectCount }}
                        </el-text>
                        <el-text class="userInfo">
                            <el-icon class="el-icon-img">
                                <Pointer/>
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
import {ChatDotSquare, Pointer, Star, View} from "@element-plus/icons-vue";
import {ElAvatar} from "element-plus";
import router from "@/router/index.js";

defineProps({
    post: {
        type: Object,
        required: true
    }
})
const openArticle = async (id) => {
    console.log("文章id->", id)
    await router.push({name: 'Article', params: {id}})
}
const handleEdit = async (id) => {
    await router.push({name: 'Edit', params: {id}})
}
</script>

<style scoped>
.post-card {
    display: flex;
}

.post-picture {
    width: 150px;
    margin-right: 20px;
}

.post-context {
    flex: 1;
}

.post-header {
    margin-bottom: 10px;
}

.post-title {
    font-size: 18px;
    color: #333333;
}

.post-summary {
    margin-bottom: 10px;
    width: 800px;
}

.post-summary p {
    font-size: 14px;
    color: #666666;
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

.userInfo {
    margin: 0 10px;
    font-size: 11px;
    color: #999999;
    align-content: center;
}

.post-category {
    display: flex;
    align-items: center;
}

.controls-txt {
    background-image: linear-gradient(45deg, #e7ffdb, #f8eded, #eeeeff);
    padding: 5px 10px;
    border-radius: 15px;
    font-size: 12px;
    color: #6a7ecd;
    margin-right: 20px;
    /*去除默认的边框*/
    border: none;
}
</style>