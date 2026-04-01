<template>
    <div class="c-user-card c-user-card--compact">
        <div class="c-user-card__avatar-row">
            <el-avatar :size="70" :src="user.photo"></el-avatar>
        </div>
        <div class="c-user-card__identity c-user-card__identity--compact">
            <div class="c-user-card__name-row">
                <span class="c-user-card__name c-user-card__name--compact">{{ user.username }}</span>
                <el-tag size="small" type="info" effect="plain">已加入0年</el-tag>
            </div>
            <p class="c-user-card__bio c-user-card__bio--compact">{{ user.profile || '暂无介绍' }}</p>
        </div>
        
        <div class="c-profile-stats c-profile-stats--grid c-user-card__stats c-user-card__stats--compact">
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">文章</span>
                <span class="c-profile-stats__value">28</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">点赞</span>
                <span class="c-profile-stats__value">28</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">收藏</span>
                <span class="c-profile-stats__value">28</span>
            </div>
            <div class="c-profile-stats__item">
                <span class="c-profile-stats__label">粉丝</span>
                <span class="c-profile-stats__value">28</span>
            </div>
        </div>
    </div>
</template>

<script setup>
defineProps({
    user: {
        type: Object,
        required: true
    }
})
</script>
