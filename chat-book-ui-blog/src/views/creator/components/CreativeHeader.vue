<template>
    <div class="creative-header glass-effect">
        <div class="left-section">
            <div class="logo" @click="router.push('/')">
                <img src="@/assets/logo.svg" alt="logo" />
                <span class="brand-name">ChatBook</span>
            </div>
            <nav class="breadcrumb-nav" aria-label="创作面包屑" v-if="!isMobile">
                <div
                    v-for="(item, index) in breadcrumbItems"
                    :key="item.key"
                    class="breadcrumb-entry"
                    :class="`breadcrumb-entry--${item.key}`"
                >
                    <button
                        v-if="item.to"
                        type="button"
                        class="breadcrumb-link"
                        @click="navigateTo(item.to)"
                    >
                        {{ item.label }}
                    </button>
                    <span v-else class="breadcrumb-current">{{ item.label }}</span>
                    <span v-if="index < breadcrumbItems.length - 1" class="breadcrumb-separator">/</span>
                </div>
            </nav>
            <div class="mobile-back-btn" v-if="isMobile" @click="router.back()" style="display: flex; align-items: center; justify-content: center; width: 32px; height: 32px; cursor: pointer; border-radius: 50%; background: rgba(0,0,0,0.05);">
                <el-icon><ArrowLeft /></el-icon>
            </div>
        </div>
        
        <div class="right-section">
            <div class="page-actions">
                <slot name="actions">
                    <div class="action-btn" v-if="!isMobile">
                        <el-button type="primary" class="create-btn" @click="router.push('/text')" round>
                            <el-icon><Plus /></el-icon> 开始创作
                        </el-button>
                    </div>
                </slot>
            </div>
            
            <el-dropdown trigger="click" popper-class="creator-user-dropdown" v-if="!isMobile">
                <div class="user-profile-trigger">
                    <div class="user-profile-main">
                        <el-avatar :size="36" :src="user.photo" class="user-avatar">
                            {{ user.username?.charAt(0) }}
                        </el-avatar>
                        <div class="user-meta">
                            <span class="username">{{ user.username || '创作者' }}</span>
                        </div>
                    </div>
                    <el-icon class="trigger-arrow"><CaretBottom /></el-icon>
                </div>
                <template #dropdown>
                    <el-dropdown-menu class="profile-dropdown-menu">
                        <el-dropdown-item class="profile-summary-item" disabled>
                            <UserCard :user="user"/>
                        </el-dropdown-item>
                        <el-dropdown-item class="profile-action-item" @click="router.push('/profile')">
                            <div class="profile-action-content">
                                <el-icon><User /></el-icon>
                                <span>个人信息</span>
                            </div>
                            <span class="profile-action-hint">查看与编辑</span>
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleCommand('logout')" class="profile-action-item logout-item">
                            <div class="profile-action-content">
                                <el-icon><SwitchButton /></el-icon>
                                <span>退出登录</span>
                            </div>
                            <span class="profile-action-hint">清除当前登录</span>
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>

            <!-- Mobile Hamburger Icon -->
            <div class="mobile-menu-trigger" v-if="isMobile" @click="showMobileMenu = true">
                <el-icon :size="24"><Menu /></el-icon>
            </div>
        </div>

        <!-- Mobile Drawer Menu -->
        <el-drawer
            v-if="isMobile"
            v-model="showMobileMenu"
            title="创作中心"
            direction="ltr"
            size="260px"
            class="mobile-creative-drawer"
            :append-to-body="true"
        >
            <div style="padding: 16px; display: flex; align-items: center; justify-content: space-between; border-bottom: 1px solid var(--border-color-light);">
                <div style="display: flex; align-items: center; gap: 12px;">
                    <el-avatar :size="40" :src="user.photo">{{ user.username?.charAt(0) }}</el-avatar>
                    <div style="display: flex; flex-direction: column;">
                        <span style="font-weight: 600; font-size: 15px;">{{ user.username || '创作者' }}</span>
                        <span style="font-size: 12px; color: var(--color-danger); cursor: pointer;" @click="handleCommand('logout')">退出登录</span>
                    </div>
                </div>
            </div>
            <!-- Render the creative aside inside the drawer -->
            <CreativeAside @close="showMobileMenu = false" />
        </el-drawer>
    </div>
</template>

<script setup>
import {computed, onMounted, onUnmounted, ref} from "vue";
import {useRoute} from "vue-router";
import router from "@/router/index.js";
import UserCard from "@/views/user/components/UserCard.vue";
import {getUserBySelf} from "@/views/user/_domain/user.js";
import { Plus, CaretBottom, User, SwitchButton, Menu, ArrowLeft } from '@element-plus/icons-vue';
import CreativeAside from '@/views/creator/components/CreativeAside.vue';

const route = useRoute();

const breadcrumbMap = {
    CreativeHome: [
        { key: 'home', label: '首页', to: '/' },
        { key: 'creative', label: '创作中心' }
    ],
    Content: [
        { key: 'home', label: '首页', to: '/' },
        { key: 'creative', label: '创作中心', to: '/creative' },
        { key: 'content', label: '内容管理' }
    ],
    Drafts: [
        { key: 'home', label: '首页', to: '/' },
        { key: 'creative', label: '创作中心', to: '/creative' },
        { key: 'drafts', label: '草稿箱' }
    ],
    Write: [
        { key: 'home', label: '首页', to: '/' },
        { key: 'creative', label: '创作中心', to: '/creative' },
        { key: 'editor', label: '文章编辑' }
    ],
    Edit: [
        { key: 'home', label: '首页', to: '/' },
        { key: 'creative', label: '创作中心', to: '/creative' },
        { key: 'editor', label: '文章编辑' }
    ]
};

const breadcrumbItems = computed(() => {
    const currentName = route.name;
    return breadcrumbMap[currentName] || breadcrumbMap.CreativeHome;
});

const navigateTo = (target) => {
    if (target && target !== route.path) {
        router.push(target);
    }
};

const handleCommand = (command) => {
    switch (command) {
        case 'logout':
            localStorage.removeItem('token');
            localStorage.removeItem('avatar');
            router.push('/login');
            break;
        default:
            break;
    }
};

const user = ref({})
const userRequest = async () => {
    try {
        const res = await getUserBySelf()
        if (res) {
            user.value = res
        }
    } catch (e) {
        console.error(e)
    }
}

const isMobile = ref(false);
const showMobileMenu = ref(false);

const checkViewport = () => {
    isMobile.value = window.innerWidth <= 768;
    if (!isMobile.value) {
        showMobileMenu.value = false;
    }
};

onMounted(() => {
    userRequest();
    checkViewport();
    window.addEventListener('resize', checkViewport);
})

onUnmounted(() => {
    window.removeEventListener('resize', checkViewport);
})
</script>

<style scoped>
.creative-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 32px;
    height: var(--header-height);
    background: linear-gradient(180deg, rgba(255, 255, 255, 0.82), rgba(255, 255, 255, 0.68) 100%);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.56);
    box-shadow: 0 14px 40px rgba(15, 23, 42, 0.04);
    position: sticky;
    top: 0;
    z-index: 100;
    transition: all 0.3s ease;
}

.left-section {
    display: flex;
    align-items: center;
    gap: 20px;
    min-width: 0;
}

.logo {
    display: flex;
    align-items: center;
    gap: 10px;
    cursor: pointer;
    transition: transform 0.2s ease;
    flex-shrink: 0;
}

.logo:active {
    transform: scale(0.98);
}

.logo img {
    height: 36px;
    width: auto;
}

.brand-name {
    font-size: 17px;
    font-weight: 700;
    color: var(--text-color-primary);
    letter-spacing: -0.03em;
}

.breadcrumb-nav {
    min-width: 0;
    display: flex;
    align-items: center;
    gap: 8px;
    overflow: hidden;
}

.breadcrumb-entry {
    display: flex;
    align-items: center;
    gap: 8px;
    min-width: 0;
}

.breadcrumb-link,
.breadcrumb-current {
    max-width: 180px;
    height: 32px;
    padding: 0 10px;
    border-radius: 999px;
    display: inline-flex;
    align-items: center;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
    font-size: 13px;
    line-height: 1;
}

.breadcrumb-link {
    border: none;
    background: transparent;
    color: var(--text-color-secondary);
    cursor: pointer;
    transition: var(--transition-base);
}

.breadcrumb-link:hover,
.breadcrumb-link:focus-visible {
    background: rgba(79, 70, 229, 0.08);
    color: var(--color-primary);
}

.breadcrumb-current {
    background: rgba(79, 70, 229, 0.1);
    color: var(--text-color-primary);
    font-weight: 700;
}

.breadcrumb-separator {
    flex-shrink: 0;
    color: rgba(148, 163, 184, 0.9);
    font-size: 12px;
}

.right-section {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px;
    border-radius: 999px;
    border: 1px solid rgba(148, 163, 184, 0.16);
    background: linear-gradient(135deg, rgba(255, 255, 255, 0.82), rgba(248, 250, 252, 0.72));
    box-shadow: 0 14px 32px rgba(15, 23, 42, 0.06);
}

.page-actions {
    display: flex;
    align-items: center;
    justify-content: flex-end;
    gap: 8px;
    padding-right: 8px;
    border-right: 1px solid rgba(148, 163, 184, 0.16);
}

.create-btn {
    height: 40px;
    padding: 0 18px;
    border-radius: 999px;
    border: 1px solid rgba(79, 70, 229, 0.16);
    background: rgba(79, 70, 229, 0.1);
    color: var(--color-primary);
    font-weight: 600;
    box-shadow: none;
    transition: var(--transition-base);
}

.create-btn:hover {
    background: rgba(79, 70, 229, 0.14);
    border-color: rgba(79, 70, 229, 0.22);
    color: var(--text-color-primary);
}

.user-profile-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 10px;
    min-height: 40px;
    min-width: 172px;
    padding: 4px 8px 4px 6px;
    border-radius: 999px;
    border: 1px solid transparent;
    background: transparent;
    box-shadow: none;
    transition: var(--transition-base);
}

.user-profile-trigger:hover {
    border-color: rgba(79, 70, 229, 0.14);
    background: rgba(79, 70, 229, 0.06);
}

.user-profile-main {
    display: flex;
    align-items: center;
    gap: 10px;
    min-width: 0;
}

.user-avatar {
    border: 2px solid rgba(255, 255, 255, 0.96);
    box-shadow: 0 8px 18px rgba(15, 23, 42, 0.12);
    flex-shrink: 0;
}

.user-meta {
    display: flex;
    flex-direction: column;
    min-width: 0;
}

.username {
    font-size: 14px;
    font-weight: 600;
    color: var(--text-color-primary);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
}

.trigger-arrow {
    width: 24px;
    height: 24px;
    display: inline-flex;
    align-items: center;
    justify-content: center;
    border-radius: 50%;
    background: rgba(79, 70, 229, 0.08);
    color: var(--text-color-secondary);
    transition: var(--transition-base);
}

.user-profile-trigger:hover .trigger-arrow {
    background: rgba(79, 70, 229, 0.12);
    color: var(--color-primary);
}

.logout-item {
    color: var(--color-danger);
}

.mobile-menu-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
    color: var(--text-color-primary);
    padding: 8px;
    border-radius: 8px;
    transition: background-color 0.2s;
}

.mobile-menu-trigger:hover {
    background: rgba(79, 70, 229, 0.08);
}

:deep(.creator-user-dropdown.el-popper) {
    padding: 8px;
    border: none;
    border-radius: 24px;
    background: rgba(255, 255, 255, 0.8);
    box-shadow: 0 28px 64px rgba(15, 23, 42, 0.18);
    backdrop-filter: blur(20px);
    -webkit-backdrop-filter: blur(20px);
}

:deep(.creator-user-dropdown.el-popper .el-popper__arrow::before) {
    border: 1px solid rgba(255, 255, 255, 0.68);
    background: rgba(255, 255, 255, 0.92);
}

:deep(.creator-user-dropdown .profile-dropdown-menu) {
    min-width: 340px;
    padding: 0;
    background: transparent;
}

:deep(.creator-user-dropdown .profile-summary-item) {
    height: auto;
    line-height: normal;
    padding: 0;
    margin-bottom: 10px;
    border-radius: 20px;
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.94), rgba(255, 255, 255, 0.82));
}

:deep(.creator-user-dropdown .profile-summary-item.is-disabled) {
    opacity: 1;
    cursor: default;
}

:deep(.creator-user-dropdown .profile-summary-item.is-disabled:hover) {
    background: linear-gradient(180deg, rgba(248, 250, 252, 0.94), rgba(255, 255, 255, 0.82));
}

:deep(.creator-user-dropdown .profile-summary-item .user-card-widget) {
    padding: 18px;
    background: transparent;
}

:deep(.creator-user-dropdown .profile-summary-item .avatar-area) {
    margin-bottom: 14px;
}

:deep(.creator-user-dropdown .profile-summary-item .avatar) {
    border: 3px solid rgba(255, 255, 255, 0.92);
    box-shadow: 0 16px 30px rgba(79, 70, 229, 0.14);
}

:deep(.creator-user-dropdown .profile-summary-item .user-details) {
    margin-bottom: 18px;
    padding-bottom: 14px;
    border-bottom-color: rgba(148, 163, 184, 0.2);
}

:deep(.creator-user-dropdown .profile-summary-item .bio) {
    line-height: 1.6;
}

:deep(.creator-user-dropdown .profile-summary-item .stats-grid) {
    gap: 10px;
}

:deep(.creator-user-dropdown .profile-summary-item .stat-item) {
    padding: 10px 8px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.76);
    border: 1px solid rgba(148, 163, 184, 0.12);
}

:deep(.creator-user-dropdown .profile-action-item) {
    min-height: 52px;
    line-height: normal;
    padding: 0 16px;
    border-radius: 18px;
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    color: var(--text-color-primary);
}

:deep(.creator-user-dropdown .profile-action-item:not(.is-disabled):hover) {
    background: rgba(79, 70, 229, 0.08);
    color: var(--color-primary);
}

:deep(.creator-user-dropdown .profile-action-item.el-dropdown-menu__item--divided) {
    margin-top: 8px;
    border-top-color: rgba(148, 163, 184, 0.16);
}

:deep(.creator-user-dropdown .logout-item:not(.is-disabled):hover) {
    background: rgba(239, 68, 68, 0.08);
    color: var(--color-danger);
}

.profile-action-content {
    display: flex;
    align-items: center;
    gap: 10px;
    font-weight: 600;
}

.profile-action-hint {
    font-size: 12px;
    color: var(--text-color-secondary);
}

@media (max-width: 960px) {
    .creative-header {
        padding: 0 16px;
    }

    .left-section {
        gap: 12px;
    }

    .brand-name {
        font-size: 16px;
    }

    .breadcrumb-entry--home {
        display: none;
    }

    .breadcrumb-link,
    .breadcrumb-current {
        max-width: 112px;
        padding: 0 8px;
    }

    .right-section {
        gap: 6px;
        padding: 4px;
    }

    .page-actions {
        gap: 4px;
        padding-right: 6px;
    }

    .user-profile-trigger {
        min-width: 0;
    }

    .username {
        max-width: 88px;
    }

    :deep(.creator-user-dropdown .profile-dropdown-menu) {
        min-width: 312px;
    }
}

@media (max-width: 768px) {
    .right-section {
        border-radius: 12px;
        background: transparent;
        box-shadow: none;
        border: none;
    }
}
</style>
