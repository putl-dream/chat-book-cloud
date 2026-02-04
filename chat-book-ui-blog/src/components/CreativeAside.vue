<template>
    <div class="header">
        <el-button type="danger" @click="router.push('/text')">发布文章</el-button>
    </div>
    <el-menu text-color="#fc5531" class="aside" router>
        <template v-for="(item, index) in menus" :key="index">
            <el-menu-item :index="item.url" v-if="!item.children">
                <el-icon class="icon">
                    <component :is="item.icon"/>
                </el-icon>
                <span slot="title">{{ item.name }}</span>
            </el-menu-item>
        </template>
    </el-menu>
</template>

<script setup>
import logo from '@/assets/logo.png';
import {markRaw, reactive} from 'vue';
import {HomeFilled, Monitor, Promotion, Document, Edit} from "@element-plus/icons-vue";
import router from "@/router/index.js";

const menus = reactive([
    {url: '/creative', name: '数据统计', icon: markRaw(HomeFilled)},
    {url: '/creative/content', name: '内容管理', icon: markRaw(Monitor)},
]);

</script>

<style scoped>
.header {
    display: flex; /* 使用 Flexbox 布局 */
    align-items: center; /* 垂直居中 */
    justify-content: center;
    gap: 10px; /* 可选：设置图片和文本之间的间距 */
    padding: 10px 0;
    height: 60px;
}

.header button {
    width: 70%;
    height: 40px;
    border-radius: 20px;
}

.icon {
    margin-right: 20px;
}

.aside {
    overflow-y: auto;
    overflow-x: hidden;
    border-right: none;
    padding: 0 70px;
    background-color: transparent;

}
</style>
