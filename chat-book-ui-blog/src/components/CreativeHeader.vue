<template>
    <div class="creative-header glass-effect">
        <div class="left-section">
            <div class="logo" @click="router.push('/')">
                <img src="@/assets/logo.png" alt="logo" />
                <span class="logo-text">ChatBook 创作中心</span>
            </div>
            <el-button @click.stop="router.push('/')" link class="home-btn">
                <el-icon><HomeFilled /></el-icon> 首页
            </el-button>
        </div>
        
        <div class="right-section">
            <div class="action-btn">
                <el-button type="primary" class="create-btn" @click="router.push('/text')" round>
                    <el-icon><Plus /></el-icon> 开始创作
                </el-button>
            </div>
            
            <el-dropdown trigger="click" popper-class="custom-dropdown">
                <div class="user-profile-trigger">
                    <el-avatar :size="36" :src="user.photo" class="user-avatar">
                        {{ user.username?.charAt(0) }}
                    </el-avatar>
                    <span class="username">{{ user.username }}</span>
                    <el-icon><CaretBottom /></el-icon>
                </div>
                <template #dropdown>
                    <el-dropdown-menu class="glass-dropdown">
                        <div class="dropdown-header">
                            <UserCard :user="user"/>
                        </div>
                        <el-dropdown-item @click="router.push('/profile')">
                            <el-icon><User /></el-icon> 个人信息
                        </el-dropdown-item>
                        <el-dropdown-item divided @click="handleCommand('logout')" class="logout-item">
                            <el-icon><SwitchButton /></el-icon> 退出登录
                        </el-dropdown-item>
                    </el-dropdown-menu>
                </template>
            </el-dropdown>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import router from "@/router/index.js";
import UserCard from "@/components/widget/UserCard.vue";
import {getUserBySelf} from "@/api/user.js";
import { HomeFilled, Plus, CaretBottom, User, SwitchButton } from '@element-plus/icons-vue';

const handleCommand = (command) => {
    switch (command) {
        case 'logout':
            // Logic for logout usually involves clearing token and redirecting
            // Assuming token clearing is handled elsewhere or just simple redirect here
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
onMounted(() => {
    userRequest()
})
</script>

<style scoped>
.creative-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 0 32px;
    height: var(--header-height);
    background: rgba(255, 255, 255, 0.7);
    backdrop-filter: blur(12px);
    border-bottom: 1px solid rgba(255, 255, 255, 0.5);
    position: sticky;
    top: 0;
    z-index: 100;
    transition: all 0.3s ease;
}

.left-section {
    display: flex;
    align-items: center;
    gap: 32px;
}

.logo {
    display: flex;
    align-items: center;
    gap: 12px;
    cursor: pointer;
    transition: transform 0.2s ease;
}

.logo:active {
    transform: scale(0.98);
}

.logo img {
    height: 36px;
    width: auto;
}

.logo-text {
    font-size: 18px;
    font-weight: 700;
    color: var(--text-color-primary);
    letter-spacing: -0.5px;
}

.home-btn {
    font-size: 14px;
    color: var(--text-color-secondary);
    display: flex;
    align-items: center;
    gap: 4px;
}

.home-btn:hover {
    color: var(--color-primary);
}

.right-section {
    display: flex;
    align-items: center;
    gap: 24px;
}

.create-btn {
    padding: 10px 24px;
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(37, 99, 235, 0.25);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.create-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(37, 99, 235, 0.35);
}

.user-profile-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 4px 8px;
    border-radius: 20px;
    transition: background 0.2s ease;
}

.user-profile-trigger:hover {
    background: rgba(0, 0, 0, 0.05);
}

.user-avatar {
    border: 2px solid #fff;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.username {
    font-size: 14px;
    font-weight: 500;
    color: var(--text-color-primary);
}

/* Dropdown styling handled via global CSS or deep selectors if scoped doesn't reach popper */
.dropdown-header {
    padding: 12px 16px;
}

.logout-item {
    color: var(--color-danger);
}
</style>