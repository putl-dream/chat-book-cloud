<template>
    <div class="creative-header glass-effect">
        <div class="left-section">
            <div class="logo" @click="router.push('/')">
                <img :src="logoUrl" alt="logo" />
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
            <div class="mobile-back-btn c-icon-trigger" v-if="isMobile" @click="router.back()">
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
            <div class="mobile-menu-trigger c-icon-trigger" v-if="isMobile" @click="showMobileMenu = true">
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
import logoUrl from "@/assets/logo.svg";

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
