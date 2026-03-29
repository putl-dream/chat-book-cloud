<template>
    <div class="c-user-page c-user-profile">
        <div class="c-user-page__container c-user-profile__container">
            <div class="c-user-profile__header">
                <div class="c-user-profile__cover">
                    <div class="c-user-profile__cover-overlay"></div>
                </div>
                <div class="c-glass-panel c-user-profile__hero">
                    <div class="c-user-profile__avatar-shell">
                        <el-avatar :size="100" :src="user.photo" class="c-user-profile__avatar">
                            <img :src="DEFAULT_AVATAR" alt="默认头像" />
                        </el-avatar>
                    </div>
                    <div class="c-user-profile__identity">
                        <h2 class="c-user-card__name c-user-card__name--hero">{{ user.username || '用户' }}</h2>
                        <p class="c-user-card__bio c-user-card__bio--hero">{{ user.profile || '这个人很懒，什么都没有留下' }}</p>
                        <div class="c-profile-stats c-user-profile__stats">
                            <div class="c-profile-stats__item c-profile-stats__item--interactive">
                                <span class="c-profile-stats__value">{{ user.articleCount || 0 }}</span>
                                <span class="c-profile-stats__label">文章</span>
                            </div>
                            <div class="c-profile-stats__divider"></div>
                            <div class="c-profile-stats__item c-profile-stats__item--interactive">
                                <span class="c-profile-stats__value">{{ user.fansCount || 0 }}</span>
                                <span class="c-profile-stats__label">粉丝</span>
                            </div>
                            <div class="c-profile-stats__divider"></div>
                            <div class="c-profile-stats__item c-profile-stats__item--interactive">
                                <span class="c-profile-stats__value">{{ user.followCount || 0 }}</span>
                                <span class="c-profile-stats__label">关注</span>
                            </div>
                        </div>
                    </div>
                    <div class="c-user-profile__actions">
                        <el-button type="primary" class="c-user-profile__edit-button" @click="$router.push('/profile/edit')">
                            编辑资料
                        </el-button>
                    </div>
                </div>
            </div>

            <div class="c-user-profile__content">
                <div class="c-glass-panel c-user-profile__main">
                    <el-tabs v-model="activeTab" class="c-user-profile__tabs">
                        <el-tab-pane label="我的文章" name="articles">
                            <div class="c-user-profile__article-list">
                                <div v-if="loading" class="c-loading-state c-user-profile__loading">
                                    <div class="c-spinner"></div>
                                </div>
                                <template v-else>
                                    <transition-group name="list-anim">
                                        <div v-for="(post, index) in posts" :key="post.id" class="c-user-profile__post"
                                            :style="{ '--delay': `${index * 0.05}s` }" @click="openArticle(post.id)">
                                            <ArticleCard :post="post" />
                                        </div>
                                    </transition-group>
                                    <div v-if="posts.length === 0" class="c-empty-panel c-user-profile__empty">
                                        <el-empty description="暂无文章" />
                                    </div>
                                </template>
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="收藏" name="collections">
                            <div class="c-user-profile__tab-empty">
                                <el-empty description="开发中..." />
                            </div>
                        </el-tab-pane>
                        <el-tab-pane label="关于我" name="about">
                            <div class="c-user-profile__about">
                                <h3 class="c-section-heading__title c-section-heading__title--md c-user-profile__about-title">详细介绍</h3>
                                <div class="c-user-profile__about-copy">
                                    <p>{{ user.profile || '暂无详细介绍' }}</p>
                                </div>
                            </div>
                        </el-tab-pane>
                    </el-tabs>
                </div>

                <div class="c-user-profile__aside">
                    <div class="c-glass-panel c-user-side-card">
                        <h3 class="c-panel-title">个人成就</h3>
                        <div class="c-user-achievement-list">
                            <div class="c-user-achievement">
                                <div class="c-user-achievement__icon c-user-achievement__icon--star">
                                    <el-icon>
                                        <Star />
                                    </el-icon>
                                </div>
                                <div class="c-user-achievement__content">
                                    <span class="c-user-achievement__value">{{ user.praiseCount || 0 }}</span>
                                    <span class="c-user-achievement__desc">次点赞</span>
                                </div>
                            </div>
                            <div class="c-user-achievement">
                                <div class="c-user-achievement__icon c-user-achievement__icon--view">
                                    <el-icon>
                                        <View />
                                    </el-icon>
                                </div>
                                <div class="c-user-achievement__content">
                                    <span class="c-user-achievement__value">{{ user.viewCount || 0 }}</span>
                                    <span class="c-user-achievement__desc">次阅读</span>
                                </div>
                            </div>
                        </div>
                    </div>

                    <div class="c-glass-panel c-user-side-card c-user-side-card--theme">
                        <h3 class="c-panel-title">主题风格</h3>
                        <div class="c-user-theme-list">
                            <button
                                v-for="option in themeOptions"
                                :key="option.id"
                                type="button"
                                class="c-user-theme-option"
                                :class="{ 'is-active': siteTheme === option.id }"
                                @click="setSiteTheme(option.id)">
                                <div class="c-user-theme-option__preview" :style="{ background: option.preview }">
                                    <span class="c-user-theme-option__preview-layer c-user-theme-option__preview-layer--nav"></span>
                                    <span class="c-user-theme-option__preview-layer c-user-theme-option__preview-layer--hero"></span>
                                    <span class="c-user-theme-option__preview-layer c-user-theme-option__preview-layer--paper"></span>
                                </div>
                                <div class="c-user-theme-option__copy">
                                    <div class="c-user-theme-option__name-row">
                                        <span class="c-user-theme-option__name">{{ option.name }}</span>
                                        <span v-if="option.id === DEFAULT_SITE_THEME" class="c-user-theme-option__badge">默认</span>
                                        <span v-if="siteTheme === option.id" class="c-user-theme-option__badge c-user-theme-option__badge--current">当前</span>
                                    </div>
                                    <p class="c-user-theme-option__description">{{ option.description }}</p>
                                </div>
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import { onMounted } from 'vue';
import ArticleCard from "@/views/article/components/ArticleCard.vue";
import { Star, View } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import { DEFAULT_AVATAR } from "@/constants/index.js";
import { useProfileLogic } from "./Profile/_hooks/useProfileLogic.js";
import { DEFAULT_SITE_THEME, useSiteTheme } from '@/composables/useSiteTheme.js';

const openArticle = (id) => {
    router.push({ name: 'Article', params: { id } });
};

const {
    user,
    posts,
    activeTab,
    loading,
    fetchUserData,
    fetchUserPosts
} = useProfileLogic();

const {
    siteTheme,
    themeOptions,
    setSiteTheme
} = useSiteTheme();

onMounted(async () => {
    await fetchUserData();
    await fetchUserPosts();
});
</script>
