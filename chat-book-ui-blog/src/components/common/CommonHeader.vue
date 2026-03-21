<template>
    <el-menu class="el-menu" mode="horizontal" :ellipsis="false" router>
        <div class="logo" @click="router.push('/')">
            <img src="@/assets/logo.svg" alt="Logo">
        </div>

        <div class="nav-links">
            <template v-for="(item, index) in menusLife" :key="index">
                <el-menu-item :index="item.url">
                    <span slot="title">{{ item.name }}</span>
                </el-menu-item>
            </template>
        </div>

        <div class="search-container" v-if="showSearch">
            <div class="search-bar" @keyup.enter="handleSearch">
                <input type="text" v-model="keyValue" placeholder="搜索..." />
                <button @click="handleSearch">搜索</button>
            </div>
        </div>
        <div class="flex-spacer" v-else></div>

        <div class="right-actions">
            <div class="user-info" v-if="user.id">
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
            <div class="user-info" v-else>
                <el-button type="primary" link @click="router.push('/login')">登录 / 注册</el-button>
            </div>

            <div class="nav-links right-nav">
                <template v-for="(item, index) in menusRight" :key="index">
                    <el-menu-item :index="item.url">
                        <span slot="title">{{ item.name }}</span>
                    </el-menu-item>
                </template>
            </div>

            <div class="action-btn">
                <el-button type="primary" icon="Plus" @click="router.push('/text')">创作</el-button>
            </div>
        </div>
    </el-menu>
</template>

<script setup>
import { markRaw, onMounted, reactive, ref } from "vue";
import { HomeFilled, Monitor, Promotion, Reading, Connection, PriceTag, ChatDotRound, Bell, Clock, Edit } from "@element-plus/icons-vue";
import router from "@/router/index.js";
import UserCard from "@/views/user/components/UserCard.vue";
import { getUserBySelf } from "@/views/user/_domain/user.js";

const props = defineProps({
    showSearch: {
        type: Boolean,
        default: true
    }
});

const keyValue = ref('')

const handleSearch = async () => {
    const key = keyValue.value
    await router.push({ name: 'List', params: { keyValue: key } })
};


const handleCommand = (command) => {
    switch (command) {
        case 'logout':
            console.log('退出登录');
            localStorage.removeItem('token');
            localStorage.removeItem('avatar');
            router.push('/login')
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
onMounted(() => {
    userRequest()
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

/* Responsive Design */
@media (max-width: 1024px) {
    .nav-links {
        display: none;
        /* Hide nav links on tablets/mobile for now, or move to hamburger */
    }

    .search-container {
        margin: 0 10px;
    }
}

@media (max-width: 768px) {
    .search-container {
        display: none;
        /* Hide search on mobile */
    }

    .right-actions .nav-links {
        display: none;
    }
}
</style>
