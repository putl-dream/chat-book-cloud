<template>
    <!-- 数据展示卡片 -->
    <div class="stats-container">
        <div class="stat-card">
            <el-text class="stat-label">总阅读量</el-text>
            <p class="stat-value">{{ totalViews }}</p>
        </div>
        <div class="stat-card">
            <el-text class="stat-label">粉丝数</el-text>
            <p class="stat-value">{{ followers }}</p>
        </div>
        <div class="stat-card">
            <el-text class="stat-label">收藏数</el-text>
            <p class="stat-value">{{ totalFavorites }}</p>
        </div>
        <div class="stat-card">
            <el-text class="stat-label">热度</el-text>
            <p class="stat-value">{{hot }}</p>
        </div>
    </div>
</template>

<script setup>
// 数据展示卡片数据
import {ref} from "vue";

const totalViews = ref(10000);
const followers = ref(500);
const totalFavorites = ref(200);
const hot = ref(85);
</script>

<style scoped>
.stats-container {
    display: flex;
    justify-content: space-around;
}

.stat-card {
    padding: 20px;
    border-radius: 8px;
    text-align: center;
    width: 150px;
}

.stat-label {
    font-weight: bold;
    margin-bottom: 10px;
}

.stat-value {
    margin-top: 10px;
    font-size: 1.2em;
    font-weight: bold;
}
</style>