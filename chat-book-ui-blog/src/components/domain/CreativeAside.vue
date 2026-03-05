<template>
    <div class="creative-aside">
        <div class="aside-header">
            <el-button class="publish-btn" type="primary" @click="router.push('/text')">
                <el-icon><EditPen /></el-icon> 发布文章
            </el-button>
        </div>
        <el-menu :default-active="$route.path" class="glass-menu" router>
            <template v-for="(item, index) in menus" :key="index">
                <el-menu-item :index="item.url" v-if="!item.children" class="menu-item">
                    <el-icon class="icon">
                        <component :is="item.icon"/>
                    </el-icon>
                    <span slot="title">{{ item.name }}</span>
                </el-menu-item>
            </template>
        </el-menu>
    </div>
</template>

<script setup>
import {markRaw, reactive} from 'vue';
import {HomeFilled, Monitor, EditPen} from "@element-plus/icons-vue";
import router from "@/router/index.js";

const menus = reactive([
    {url: '/creative', name: '数据统计', icon: markRaw(HomeFilled)},
    {url: '/creative/content', name: '内容管理', icon: markRaw(Monitor)},
]);

</script>

<style scoped>
.creative-aside {
    height: 100%;
    padding: 20px 10px;
}

.aside-header {
    display: flex;
    justify-content: center;
    margin-bottom: 24px;
    padding: 0 10px;
}

.publish-btn {
    width: 100%;
    height: 44px;
    border-radius: 12px;
    font-size: 16px;
    font-weight: 600;
    background: linear-gradient(135deg, var(--color-primary) 0%, var(--color-primary-hover) 100%);
    border: none;
    box-shadow: 0 4px 10px rgba(37, 99, 235, 0.3);
    transition: all 0.3s ease;
}

.publish-btn:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 14px rgba(37, 99, 235, 0.4);
}

.publish-btn .el-icon {
    margin-right: 8px;
}

.glass-menu {
    border-right: none;
    background-color: transparent;
}

.menu-item {
    margin-bottom: 8px;
    border-radius: 10px;
    height: 50px;
    line-height: 50px;
    color: var(--text-color-regular);
    transition: all 0.3s ease;
}

.menu-item:hover {
    background-color: rgba(255, 255, 255, 0.5);
    color: var(--color-primary);
}

.menu-item.is-active {
    background-color: var(--bg-color-white);
    color: var(--color-primary);
    font-weight: 600;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
}

.menu-item .icon {
    margin-right: 12px;
    font-size: 18px;
}
</style>
