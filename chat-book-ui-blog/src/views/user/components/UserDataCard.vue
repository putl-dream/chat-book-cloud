<template>
    <div class="c-user-metric-grid">
        <div class="c-user-metric-card">
            <el-text class="c-user-metric-card__label">总阅读量</el-text>
            <p class="c-user-metric-card__value">{{ totalViews }}</p>
        </div>
        <div class="c-user-metric-card">
            <el-text class="c-user-metric-card__label">粉丝数</el-text>
            <p class="c-user-metric-card__value">{{ followers }}</p>
        </div>
        <div class="c-user-metric-card">
            <el-text class="c-user-metric-card__label">收藏数</el-text>
            <p class="c-user-metric-card__value">{{ totalFavorites }}</p>
        </div>
        <div class="c-user-metric-card">
            <el-text class="c-user-metric-card__label">热度</el-text>
            <p class="c-user-metric-card__value">{{ hot }}</p>
        </div>
    </div>
</template>

<script setup>
import { ref } from "vue";

const totalViews = ref(10000);
const followers = ref(500);
const totalFavorites = ref(200);
const hot = ref(85);
</script>
