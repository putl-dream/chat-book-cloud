<template>
    <div class="profile-page">
        <div class="profile-container">
            <div class="profile-header">
                <div class="user-cover">
                    <div class="cover-overlay"></div>
                </div>
                <div class="user-info-card glass-panel">
                    <div class="avatar-wrapper">
                        <el-avatar :size="100" :src="user.photo" class="user-avatar">
                            <img :src="DEFAULT_AVATAR" />
                        </el-avatar>
                        <div class="avatar-border"></div>
                    </div>
                    <div class="info-content">
                        <h2 class="username">{{ user.username || '用户' }}</h2>
                        <p class="bio">{{ user.profile || '这个人很懒，什么都没有留下' }}</p>
                        <div class="stats">
                            <div class="stat-item">
                                <span class="count">{{ user.articleCount || 0 }}</span>
                                <span class="label">文章</span>
                            </div>
                            <div class="stat-divider"></div>
                            <div class="stat-item">
                                <span class="count">{{ user.fansCount || 0 }}</span>
                                <span class="label">粉丝</span>
                            </div>
                            <div class="stat-divider"></div>
                            <div class="stat-item">
                                <span class="count">{{ user.followCount || 0 }}</span>
                                <span class="label">关注</span>
                            </div>
                        </div>
                    </div>
                    <div class="actions">
                        <el-button type="primary" class="edit-btn" @click="$router.push('/profile/edit')">
                            编辑资料
                        </el-button>
                    </div>
                </div>
            </div>

            <div class="profile-content">
                <div class="content-left glass-panel">
                    <el-tabs v-model="activeTab" class="profile-tabs">
                        <el-tab-pane label="我的文章" name="articles">
                            <div class="article-list">
                                <div v-if="loading" class="loading">
                                    <div class="spinner"></div>
                                </div>
                                <template v-else>
                                    <transition-group name="list-anim">
                                        <div v-for="(post, index) in posts" :key="post.id" class="post-item-wrapper"
                                            :style="{ '--delay': `${index * 0.05}s` }" @click="openArticle(post.id)">
                                            <ArticleCard :post="post" />
                                        </div>
                                    </transition-group>
                                    <div v-if="posts.length === 0" class="empty-state">
                                        <el-empty description="暂无文章" />
                                    </div>
                                </template>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="收藏" name="collections">
                            <div class="empty-tab">
                                <el-empty description="开发中..." />
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="关于我" name="about">
                            <div class="about-section">
                                <h3 class="section-title">详细介绍</h3>
                                <div class="about-content">
                                    <p>{{ user.profile || '暂无详细介绍' }}</p>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>

                <div class="content-right">
                    <div class="side-card glass-panel">
                        <h3 class="side-title">个人成就</h3>
                        <div class="achievement-list">
                            <div class="achievement-item">
                                <div class="icon-box star">
                                    <el-icon>
                                        <Star />
                                    </el-icon>
                                </div>
                                <div class="achievement-text">
                                    <span class="value">{{ user.praiseCount || 0 }}</span>
                                    <span class="desc">次点赞</span>
                                </div>
                            </div>
                            <div class="achievement-item">
                                <div class="icon-box view">
                                    <el-icon>
                                        <View />
                                    </el-icon>
                                </div>
                                <div class="achievement-text">
                                    <span class="value">{{ user.viewCount || 0 }}</span>
                                    <span class="desc">次阅读</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { getUserBySelf } from "@/api/user.js";
import { getUserArticlePage } from "@/api/article.js";
import ArticleCard from "@/components/widget/ArticleCard.vue";
import { Star, View } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import { DEFAULT_AVATAR } from "@/constants/index.js";

const user = ref({});
const posts = ref([]);
const activeTab = ref('articles');
const loading = ref(false);

const fetchUserData = async () => {
    try {
        const res = await getUserBySelf();
        // Axios 拦截器已经处理了 CommonResult，并返回了 .data
        if (res) {
            user.value = res;
        }
    } catch (error) {
        console.error('获取用户信息失败', error);
    }
};

const fetchUserPosts = async () => {
    loading.value = true;
    try {
        // 确保 userId 存在
        if (!user.value || !user.value.id) {
            console.warn('User ID is missing, cannot fetch posts');
            return;
        }
        const res = await getUserArticlePage(1, 10, user.value.id);
        if (res && res.list) {
            posts.value = res.list;
        }
    } catch (error) {
        console.error('获取文章失败', error);
    } finally {
        loading.value = false;
    }
};

const openArticle = (id) => {
    router.push({ name: 'Article', params: { id } });
};

onMounted(async () => {
    await fetchUserData();
    await fetchUserPosts();
});
</script>

<style scoped>
.profile-page {
    min-height: 100%;
    background-color: var(--bg-color-base);
    padding-bottom: 60px;
    background: radial-gradient(circle at top right, #eef2ff, var(--bg-color-base));
}

.profile-container {
    max-width: var(--container-width-lg);
    margin: 0 auto;
    padding: 0 20px;
}

.user-cover {
    height: 175px;
    background: linear-gradient(120deg, #a5f3fc, #c4b5fd, #fbcfe8);
    background-size: 200% 200%;
    animation: gradientBG 10s ease infinite;
    border-radius: 0 0 24px 24px;
    position: relative;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.05);
}

@keyframes gradientBG {
    0% {
        background-position: 0% 50%;
    }

    50% {
        background-position: 100% 50%;
    }

    100% {
        background-position: 0% 50%;
    }
}

.cover-overlay {
    position: absolute;
    inset: 0;
    background: linear-gradient(to bottom, transparent, rgba(255, 255, 255, 0.3));
}

.glass-panel {
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(20px);
    border: 1px solid rgba(255, 255, 255, 0.8);
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.05);
    border-radius: 24px;
}

.user-info-card {
    padding: 24px 32px;
    margin-top: -60px;
    position: relative;
    display: flex;
    align-items: flex-end;
    gap: 32px;
    margin-bottom: 32px;
    margin-left: 20px;
    margin-right: 20px;
}

.avatar-wrapper {
    margin-top: -80px;
    padding: 6px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(10px);
    border-radius: 50%;
    position: relative;
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.1);
}

.info-content {
    flex: 1;
    padding-bottom: 4px;
}

.username {
    font-size: 28px;
    font-weight: 800;
    color: var(--text-color-primary);
    margin: 0 0 8px;
    letter-spacing: -0.5px;
}

.bio {
    color: var(--text-color-secondary);
    margin-bottom: 20px;
    font-size: 14px;
    line-height: 1.5;
}

.stats {
    display: flex;
    align-items: center;
    gap: 24px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: transform 0.2s;
}

.stat-item:hover {
    transform: translateY(-2px);
}

.stat-divider {
    width: 1px;
    height: 24px;
    background: rgba(0, 0, 0, 0.06);
}

.count {
    font-size: 20px;
    font-weight: 700;
    color: var(--text-color-primary);
}

.label {
    font-size: 12px;
    color: var(--text-color-secondary);
    margin-top: 4px;
}

.actions {
    padding-bottom: 12px;
}

.edit-btn {
    border-radius: 20px;
    padding: 10px 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(var(--color-primary-rgb), 0.2);
    transition: all 0.3s;
}

.edit-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(var(--color-primary-rgb), 0.3);
}

.profile-content {
    display: flex;
    gap: 24px;
    padding: 0 20px;
}

.content-left {
    flex: 1;
    padding: 24px;
    min-height: 500px;
}

.content-right {
    width: 320px;
    flex-shrink: 0;
}

.side-card {
    padding: 24px;
}

.side-title {
    font-size: 18px;
    margin: 0 0 20px;
    color: var(--text-color-primary);
    font-weight: 700;
    display: flex;
    align-items: center;
}

.side-title::before {
    content: '';
    display: block;
    width: 4px;
    height: 18px;
    background: var(--color-primary);
    border-radius: 2px;
    margin-right: 10px;
}

.achievement-list {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.achievement-item {
    display: flex;
    align-items: center;
    gap: 16px;
    padding: 12px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 12px;
    transition: all 0.3s;
}

.achievement-item:hover {
    background: #fff;
    transform: translateX(4px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.icon-box {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 20px;
}

.icon-box.star {
    background: #fff7ed;
    color: #f97316;
}

.icon-box.view {
    background: #eff6ff;
    color: #3b82f6;
}

.achievement-text {
    display: flex;
    flex-direction: column;
}

.achievement-text .value {
    font-size: 16px;
    font-weight: 700;
    color: var(--text-color-primary);
}

.achievement-text .desc {
    font-size: 12px;
    color: var(--text-color-secondary);
}

.post-item-wrapper {
    margin-bottom: 16px;
    border-radius: 16px;
    transition: all 0.3s;
    animation: fadeInUp 0.5s backwards;
    animation-delay: var(--delay);
}

.post-item-wrapper:hover {
    background: rgba(255, 255, 255, 0.5);
    transform: translateY(-2px);
}

.loading {
    display: flex;
    justify-content: center;
    padding: 40px 0;
}

.spinner {
    width: 32px;
    height: 32px;
    border: 3px solid rgba(0, 0, 0, 0.1);
    border-top-color: var(--color-primary);
    border-radius: 50%;
    animation: spin 1s linear infinite;
}

.empty-state {
    padding: 60px 0;
}

.empty-tab {
    padding: 40px 0;
}

.about-section {
    padding: 10px;
}

.section-title {
    font-size: 18px;
    margin-bottom: 16px;
    color: var(--text-color-primary);
}

.about-content {
    line-height: 1.8;
    color: var(--text-color-regular);
    font-size: 15px;
}

@keyframes spin {
    to {
        transform: rotate(360deg);
    }
}

@keyframes fadeInUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

/* Responsive */
@media (max-width: 992px) {
    .profile-content {
        flex-direction: column;
    }

    .content-right {
        width: 100%;
    }
}

@media (max-width: 768px) {
    .user-info-card {
        flex-direction: column;
        align-items: center;
        text-align: center;
        margin-top: -40px;
        padding: 24px 16px;
    }

    .avatar-wrapper {
        margin-top: -60px;
    }

    .info-content {
        width: 100%;
        display: flex;
        flex-direction: column;
        align-items: center;
    }

    .stats {
        justify-content: center;
        width: 100%;
    }

    .profile-container {
        padding: 0 12px;
    }

    .profile-content {
        padding: 0;
    }
}
</style>
