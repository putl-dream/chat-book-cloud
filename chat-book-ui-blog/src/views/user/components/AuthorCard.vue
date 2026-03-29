<template>
    <div class="c-solid-panel c-user-card">
        <h3 class="c-panel-title c-panel-title--compact c-user-card__title">作者介绍</h3>
        <div class="c-user-card__identity">
            <div class="c-user-card__avatar-ring">
                <el-avatar :size="80" :src="userInfo.photo" alt="Author Avatar"></el-avatar>
            </div>
            <div>
                <p class="c-user-card__name">{{ userInfo.username || '作者' }}</p>
                <p class="c-user-card__meta">已加入707 年</p>
                <div class="c-user-card__actions">
                    <el-button type="primary" class="c-user-card__action--primary">关注作者</el-button>
                    <el-button class="c-user-card__action--secondary">查看教程</el-button>
                </div>
            </div>
        </div>
        <div class="c-profile-stats c-profile-stats--grid c-user-card__stats c-user-card__stats--section">
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">文章</span>
                <span class="c-profile-stats__value">28</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">点赞</span>
                <span class="c-profile-stats__value">1.2k</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">收藏</span>
                <span class="c-profile-stats__value">856</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">粉丝</span>
                <span class="c-profile-stats__value">432</span>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref, watch} from "vue";
import {getUserById} from "@/views/user/_domain/user.js";

const props = defineProps({
    userId: {
        required: true,
    }
});
const userInfo = ref({});

const queryUserInfo = async () => {
    if (props.userId) {
        const param = await getUserById(props.userId);
        if (param) {
            userInfo.value = param;
        }
    }
};

onMounted(queryUserInfo);
watch(() => props.userId, queryUserInfo);
</script>
