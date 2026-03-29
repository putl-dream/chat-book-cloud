<template>
    <div class="c-creator-nav">
        <div class="c-creator-nav__header">
            <el-button class="c-creator-nav__cta" type="primary" @click="router.push('/text')">
                <el-icon><EditPen /></el-icon> 发布文章
            </el-button>
        </div>
        <el-menu :default-active="$route.path" class="c-creator-nav__menu" router @select="$emit('close')">
            <template v-for="(item, index) in menus" :key="index">
                <el-menu-item :index="item.url" v-if="!item.children" class="c-creator-nav__item">
                    <el-icon class="c-creator-nav__icon">
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
import {HomeFilled, Monitor, EditPen, DocumentCopy} from "@element-plus/icons-vue";
import router from "@/router/index.js";

const menus = reactive([
    {url: '/creative', name: '数据统计', icon: markRaw(HomeFilled)},
    {url: '/creative/content', name: '内容管理', icon: markRaw(Monitor)},
    {url: '/creative/drafts', name: '草稿箱', icon: markRaw(DocumentCopy)},
]);

</script>
