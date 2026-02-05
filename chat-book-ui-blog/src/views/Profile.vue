<template>
    <div class="profile-page">
        <div class="profile-container">
            <div class="profile-header">
                <div class="user-cover"></div>
                <div class="user-info-card">
                    <div class="avatar-wrapper">
                        <el-avatar :size="100" :src="user.photo" class="user-avatar"></el-avatar>
                    </div>
                    <div class="info-content">
                        <h2 class="username">{{ user.username || '用户' }}</h2>
                        <p class="bio">{{ user.profile || '这个人很懒，什么都没有留下' }}</p>
                        <div class="stats">
                            <div class="stat-item">
                                <span class="count">{{ user.articleCount || 0 }}</span>
                                <span class="label">文章</span>
                            </div>
                            <div class="stat-item">
                                <span class="count">{{ user.fansCount || 0 }}</span>
                                <span class="label">粉丝</span>
                            </div>
                            <div class="stat-item">
                                <span class="count">{{ user.followCount || 0 }}</span>
                                <span class="label">关注</span>
                            </div>
                        </div>
                    </div>
                    <div class="actions">
                        <el-button type="primary" plain @click="$router.push('/profile/edit')">编辑资料</el-button>
                    </div>
                </div>
            </div>

            <div class="profile-content">
                <div class="content-left">
                    <el-tabs v-model="activeTab" class="profile-tabs">
                        <el-tab-pane label="我的文章" name="articles">
                            <div class="article-list">
                                <div v-if="loading" class="loading">
                                    <el-skeleton :rows="3" animated />
                                </div>
                                <template v-else>
                                    <div v-for="post in posts" :key="post.id" class="post-item"
                                        @click="openArticle(post.id)">
                                        <ArticleCard :post="post" />
                                    </div>
                                    <div v-if="posts.length === 0" class="empty-state">
                                        <el-empty description="暂无文章" />
                                    </div>
                                </template>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="收藏" name="collections">
                            <el-empty description="开发中..." />
                        </el-tab-pane>
                        <el-tab-pane label="关于我" name="about">
                            <div class="about-section">
                                <h3>详细介绍</h3>
                                <p>{{ user.profile || '暂无介绍' }}</p>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>

                <div class="content-right">
                    <div class="side-card">
                        <h3>个人成就</h3>
                        <div class="achievement-list">
                            <div class="achievement-item">
                                <el-icon>
                                    <Star />
                                </el-icon>
                                <span>获得 {{ user.praiseCount || 0 }} 次点赞</span>
                            </div>
                            <div class="achievement-item">
                                <el-icon>
                                    <View />
                                </el-icon>
                                <span>文章被阅读 {{ user.viewCount || 0 }} 次</span>
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
    min-height: 100vh;
    background-color: var(--bg-color-base);
    padding-bottom: 40px;
}

.user-cover {
    height: 200px;
    background: linear-gradient(120deg, var(--color-primary-light), #e0c3fc);
}

.profile-container {
    max-width: var(--container-width-lg);
    margin: 0 auto;
    padding: 0 20px;
}

.user-info-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 20px;
    margin-top: -50px;
    position: relative;
    box-shadow: var(--box-shadow-base);
    display: flex;
    align-items: flex-end;
    gap: 24px;
    margin-bottom: 24px;
}

.avatar-wrapper {
    margin-top: -50px;
    padding: 4px;
    background: var(--bg-color-white);
    border-radius: 50%;
}

.info-content {
    flex: 1;
    padding-bottom: 10px;
}

.username {
    font-size: 24px;
    font-weight: 600;
    color: var(--text-color-primary);
    margin-bottom: 8px;
}

.bio {
    color: var(--text-color-secondary);
    margin-bottom: 16px;
    font-size: 14px;
}

.stats {
    display: flex;
    gap: 24px;
}

.stat-item {
    display: flex;
    flex-direction: column;
    align-items: center;
}

.count {
    font-size: 18px;
    font-weight: 600;
    color: var(--text-color-primary);
}

.label {
    font-size: 12px;
    color: var(--text-color-secondary);
}

.actions {
    padding-bottom: 10px;
}

.profile-content {
    display: flex;
    gap: 24px;
}

.content-left {
    flex: 1;
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 20px;
    min-height: 400px;
    box-shadow: var(--box-shadow-base);
}

.content-right {
    width: 300px;
    flex-shrink: 0;
}

.side-card {
    background: var(--bg-color-white);
    border-radius: var(--border-radius-large);
    padding: 20px;
    box-shadow: var(--box-shadow-base);
}

.side-card h3 {
    font-size: 16px;
    margin-bottom: 16px;
    color: var(--text-color-primary);
    border-left: 4px solid var(--color-primary);
    padding-left: 10px;
}

.achievement-item {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;
    color: var(--text-color-regular);
    font-size: 14px;
}

.post-item {
    border-bottom: 1px solid var(--border-color-lighter);
    padding: 16px 0;
    cursor: pointer;
    transition: background-color 0.2s;
}

.post-item:hover {
    background-color: var(--bg-color-base);
}

.post-item:last-child {
    border-bottom: none;
}

/* Responsive */
@media (max-width: 768px) {
    .user-info-card {
        flex-direction: column;
        align-items: center;
        text-align: center;
        margin-top: -30px;
    }

    .avatar-wrapper {
        margin-top: -60px;
    }

    .info-content {
        width: 100%;
    }

    .stats {
        justify-content: center;
    }

    .profile-content {
        flex-direction: column;
    }

    .content-right {
        width: 100%;
    }
}
</style>