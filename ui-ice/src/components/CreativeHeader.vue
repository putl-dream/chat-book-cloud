<template>
    <div class="creative-header">
        <div class="logo" @click="router.push('/')">
            <img src="@/assets/logo.png" alt="logo" />
            <el-button @click.stop="router.push('/')" text class="home-btn"> >> 首页 </el-button>
        </div>
        <div class="right">
            <el-dropdown trigger="click">
                <span class="user-avatar-trigger">
                    <el-avatar :size="40" :src="user.photo" class="user-avatar"></el-avatar>
                </span>
                <template #dropdown>
                    <div class="dropdown-content">
                        <UserCard :user="user"/>
                        <el-dropdown-item class="centered-item" @click="router.push('/profile')">
                            个人信息
                        </el-dropdown-item>
                        <el-dropdown-item @click="handleCommand('logout')">
                            退出登录
                        </el-dropdown-item>
                    </div>
                </template>
            </el-dropdown>
            <div class="action-btn">
                <el-button type="primary" icon="Plus" @click="router.push('/text')">创作</el-button>
            </div>
        </div>
    </div>
</template>

<script setup>
import {onMounted, ref} from "vue";
import router from "@/router/index.js";
import UserCard from "@/components/widget/UserCard.vue";
import {getUserBySelf} from "@/api/user.js";

const handleCommand = (command) => {
    switch (command) {
        case 'logout':
            console.log('退出登录');
            router.push('/login')
            break;
        default:
            break;
    }
};

const user = ref({})
const userRequest = async () => {
    const params = await getUserBySelf()
    if (params.code === 200) {
        user.value = params.data
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
    padding: 0 20px;
    height: var(--header-height);
    background: var(--bg-color-white);
    box-shadow: var(--box-shadow-base);
    border-bottom: 1px solid var(--border-color-light);
}

.logo {
    display: flex;
    align-items: center;
    cursor: pointer;
}

.logo img {
    height: 40px;
}

.home-btn {
    font-size: 16px;
    padding: 0 20px;
    color: var(--text-color-regular);
}

.home-btn:hover {
    color: var(--color-primary);
}

.right {
    display: flex;
    align-items: center;
    gap: 20px;
}

.user-avatar-trigger {
    cursor: pointer;
    display: flex;
    align-items: center;
}

.dropdown-content {
    width: 260px;
    padding: 10px;
    background: var(--bg-color-white);
}

.action-btn {
    margin-left: 10px;
}
</style>