<template>
    <el-menu class="el-menu" mode="horizontal" :ellipsis="false" router>
        <div class="logo" @click="router.push('/')">
            <img src="@/assets/logo.svg" alt="Logo">
        </div>

        <div class="nav-links" v-if="!isMobile">
            <template v-for="(item, index) in menusLife" :key="index">
                <el-menu-item :index="item.url">
                    <span slot="title">{{ item.name }}</span>
                </el-menu-item>
            </template>
        </div>

        <div class="search-container" v-if="showSearch && !isMobile">
            <div class="search-bar" @keyup.enter="handleSearch">
                <input type="text" v-model="keyValue" placeholder="搜索..." />
                <button @click="handleSearch">搜索</button>
            </div>
        </div>
        <div class="flex-spacer" v-else-if="!isMobile"></div>
        <div class="flex-spacer" v-if="isMobile"></div>

        <div class="right-actions">
            <!-- Mobile Search Icon -->
            <div class="mobile-search-trigger" v-if="isMobile && showSearch" @click="showMobileSearch = !showMobileSearch">
                <el-icon :size="20"><Search /></el-icon>
            </div>

            <div class="user-info" v-if="user.id && !isMobile">
                <el-dropdown trigger="click">
                    <span class="user-avatar-trigger">
                        <el-avatar class="user-avatar" :size="36" :src="user.photo"></el-avatar>
                    </span>
                    <template #dropdown>
                        <div class="dropdown-content">
                            <UserCard :user="user" />
                            <el-dropdown-item class="centered-item" @click="router.push('/profile')">
                                个人信息
                            </el-dropdown-item>
                            <el-dropdown-item @click="handleCommand('logout')">
                                退出登录
                            </el-dropdown-item>
                        </div>
                    </template>
                </el-dropdown>
            </div>
            <div class="user-info" v-else-if="!isMobile && !user.id">
                <el-button type="primary" link @click="router.push('/login')">登录 / 注册</el-button>
            </div>

            <div class="nav-links right-nav" v-if="!isMobile">
                <template v-for="(item, index) in menusRight" :key="index">
                    <el-menu-item :index="item.url">
                        <span slot="title">{{ item.name }}</span>
                    </el-menu-item>
                </template>
            </div>

            <div class="action-btn" v-if="!isMobile">
                <el-button type="primary" icon="Plus" @click="router.push('/text')">创作</el-button>
            </div>

            <!-- Mobile Hamburger Icon -->
            <div class="mobile-menu-trigger" v-if="isMobile" @click="showMobileMenu = true">
                <el-icon :size="24"><Menu /></el-icon>
            </div>
        </div>

        <!-- Mobile Search Dropdown -->
        <transition name="fade">
            <div class="mobile-search-bar" v-if="isMobile && showMobileSearch">
                <input type="text" v-model="keyValue" placeholder="搜索..." @keyup.enter="handleSearchMobile" />
                <el-button type="primary" size="small" @click="handleSearchMobile">搜索</el-button>
            </div>
        </transition>

        <!-- Mobile Drawer Menu -->
        <el-drawer
            v-if="isMobile"
            v-model="showMobileMenu"
            title="导航菜单"
            direction="ltr"
            size="260px"
            class="mobile-nav-drawer"
            :append-to-body="true"
        >
            <el-menu mode="vertical" :default-active="route?.path" router>
                <div v-if="user.id" style="padding: 16px; display: flex; align-items: center; gap: 12px;">
                    <el-avatar :size="40" :src="user.photo"></el-avatar>
                    <div style="display: flex; flex-direction: column;">
                        <span style="font-weight: 600; font-size: 15px;">{{ user.name || user.username || '用户' }}</span>
                        <span style="font-size: 12px; color: var(--text-color-secondary);">欢迎回来</span>
                    </div>
                </div>

                <template v-for="(item, index) in menusLife" :key="'life-'+index">
                    <el-menu-item :index="item.url" @click="showMobileMenu = false">
                        <el-icon class="mobile-menu-item-icon"><component :is="item.icon" /></el-icon>
                        <span slot="title">{{ item.name }}</span>
                    </el-menu-item>
                </template>
                
                <el-divider v-if="user.id" style="margin: 12px 0;" />
                
                <template v-if="user.id" v-for="(item, index) in menusRight" :key="'right-'+index">
                    <el-menu-item :index="item.url" @click="showMobileMenu = false">
                        <el-icon class="mobile-menu-item-icon"><component :is="item.icon" /></el-icon>
                        <span slot="title">{{ item.name }}</span>
                    </el-menu-item>
                </template>

                <el-menu-item v-if="user.id" index="/text" @click="showMobileMenu = false">
                    <el-icon class="mobile-menu-item-icon"><Edit /></el-icon>
                    <span slot="title">创作中心</span>
                </el-menu-item>
                <el-menu-item v-if="!user.id" index="/login" @click="showMobileMenu = false">
                    <el-icon class="mobile-menu-item-icon"><User /></el-icon>
                    <span slot="title">登录 / 注册</span>
                </el-menu-item>
                <el-menu-item v-if="user.id" index="/profile" @click="showMobileMenu = false">
                    <el-icon class="mobile-menu-item-icon"><User /></el-icon>
                    <span slot="title">个人信息</span>
                </el-menu-item>
            </el-menu>
        </el-drawer>
    </el-menu>
</template>

<script setup>
import { markRaw, onMounted, onUnmounted, reactive, ref } from "vue";
import { HomeFilled, Monitor, Promotion, Reading, Connection, PriceTag, ChatDotRound, Bell, Clock, Edit, Menu, Search, User, Plus } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import { useRoute } from "vue-router";
import UserCard from "@/views/user/components/UserCard.vue";
import { getUserBySelf } from "@/views/user/_domain/user.js";
import { logoutAndRevoke } from "@/utils/token.js";

const props = defineProps({
    showSearch: {
        type: Boolean,
        default: true
    }
});

const keyValue = ref('')
const route = useRoute();

const handleSearch = async () => {
    const key = keyValue.value
    await router.push({ name: 'List', params: { keyValue: key } })
};

const handleSearchMobile = async () => {
    if (keyValue.value) {
        showMobileSearch.value = false;
        await handleSearch();
    }
};

const handleCommand = async (command) => {
    switch (command) {
        case 'logout':
            console.log('退出登录');
            await logoutAndRevoke();
            router.push('/login');
            break;
        case 'user':
            console.log('查看个人资料');
            router.push('/chat')
            break;
        default:
            break;
    }
};

// 左侧导航 - 用户找内容的路径
const menusLife = reactive([
    { url: '/', name: '首页', icon: markRaw(HomeFilled) },
    { url: '/learn', name: '学习', icon: markRaw(Reading) },
    { url: '/practice', name: '实战', icon: markRaw(Connection) },
    { url: '/tags', name: '标签', icon: markRaw(PriceTag) },
]);

// 右侧导航 - 功能入口
const menusRight = reactive([
    { url: '/chat', name: '聊天', icon: markRaw(ChatDotRound) },
    { url: '/message', name: '消息', icon: markRaw(Bell) },
    { url: '/history', name: '历史', icon: markRaw(Clock) },
    { url: '/creative', name: '创作中心', icon: markRaw(Edit) },
]);

const user = ref({})
const userRequest = async () => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const params = await getUserBySelf()
    if (params) {
        user.value = params
        localStorage.setItem('avatar', user.value.photo)
    }
}

const isMobile = ref(false);
const showMobileMenu = ref(false);
const showMobileSearch = ref(false);

const checkViewport = () => {
    isMobile.value = window.innerWidth <= 768;
    if (!isMobile.value) {
        showMobileMenu.value = false;
        showMobileSearch.value = false;
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
.el-menu--horizontal {
    --el-menu-horizontal-height: var(--header-height);
    border-bottom: 1px solid var(--app-header-border);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 40px;
    background: var(--app-header-bg);
    backdrop-filter: var(--blur-base);
    -webkit-backdrop-filter: var(--blur-base);
    position: sticky;
    top: 0;
    z-index: 2000;
    transition: var(--transition-base);
}

.el-menu--horizontal:hover {
    background: var(--app-header-bg-hover);
}

.logo {
    margin-right: 20px;
    cursor: pointer;
    display: flex;
    align-items: center;
}

.logo img {
    height: 40px;
}

.nav-links {
    display: flex;
    flex-wrap: nowrap;
}

.search-container {
    flex: 1;
    display: flex;
    justify-content: center;
    margin: 0 20px;
}

.flex-spacer {
    flex: 1;
}

.search-bar {
    display: flex;
    border: 1px solid var(--border-color-light);
    border-radius: var(--border-radius-large);
    overflow: hidden;
    height: 40px;
    width: 100%;
    max-width: 480px;
    background: var(--app-search-bg);
    transition: var(--transition-base);
}

.search-bar:hover {
    border-color: var(--border-color-base);
}

.search-bar:focus-within {
    border-color: var(--color-primary);
    background: var(--app-search-bg-focus);
    box-shadow: 0 0 0 4px var(--app-search-ring);
}

.search-bar input {
    border: none;
    padding: 0 16px;
    flex: 1;
    outline: none;
    color: var(--text-color-primary);
    background: transparent;
    font-size: 14px;
}

.search-bar button {
    width: 60px;
    background-color: transparent;
    color: var(--color-primary);
    border: none;
    cursor: pointer;
    font-size: 14px;
    font-weight: 500;
    transition: var(--transition-fast);
}

.search-bar button:hover {
    color: var(--color-primary-hover);
    background: var(--color-primary-light);
}

.right-actions {
    display: flex;
    align-items: center;
    gap: 16px;
}

.user-avatar-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.dropdown-content {
    width: 260px;
    padding: 10px;
    background: var(--app-dropdown-bg);
    border: 1px solid var(--border-color-light);
    border-radius: 20px;
    box-shadow: var(--box-shadow-hover);
}

.action-btn {
    margin-left: 10px;
}

/* Mobile Search Icon & Menu Triggers */
.mobile-search-trigger, .mobile-menu-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
    color: var(--text-color-primary);
    padding: 8px;
    border-radius: 8px;
    transition: background-color 0.2s;
}

.mobile-search-trigger:hover, .mobile-menu-trigger:hover {
    background-color: var(--bg-color-hover, rgba(0,0,0,0.05));
}

.mobile-search-bar {
    position: absolute;
    top: var(--header-height, 60px);
    left: 0;
    right: 0;
    padding: 12px 20px;
    background: var(--app-header-bg);
    border-bottom: 1px solid var(--border-color-light);
    display: flex;
    gap: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
    z-index: 1000;
}

.mobile-search-bar input {
    flex: 1;
    border: 1px solid var(--border-color-base);
    border-radius: 20px;
    padding: 0 16px;
    height: 36px;
    outline: none;
    font-size: 14px;
}

.mobile-search-bar input:focus {
    border-color: var(--color-primary);
}

.mobile-menu-item-icon {
    margin-right: 12px;
}

.fade-enter-active, .fade-leave-active {
    transition: opacity 0.2s, transform 0.2s;
}
.fade-enter-from, .fade-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}

/* Responsive Design */
@media (max-width: 1024px) {
    .nav-links {
        display: none;
    }

    .search-container {
        margin: 0 10px;
    }
}

@media (max-width: 768px) {
    .el-menu--horizontal {
        padding: 0 16px;
    }

    .search-container {
        display: none;
    }

    .right-actions .nav-links,
    .right-actions .action-btn,
    .right-actions .user-info {
        display: none;
    }
}
</style>
