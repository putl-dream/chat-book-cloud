<template>
    <el-container class="page">
        <el-header class="header">
            <CommonHeader/>
        </el-header>

        <el-main class="main" id="common-layout-main">
            <div class="main-view">
                <router-view v-slot="{ Component }">
                    <component :is="Component" class="route-view" />
                </router-view>
            </div>
            <SiteFooter class="layout-footer" />
        </el-main>
    </el-container>
</template>

<script setup>
import CommonHeader from "@/components/common/CommonHeader.vue";
import SiteFooter from "@/components/common/SiteFooter.vue";
</script>

<style scoped>

.main {
    padding: 0;
    width: 100%;
    flex: 1;
    min-height: 0;
    overflow-y: auto;
    overflow-x: hidden;
    display: flex;
    flex-direction: column;
}

.main-view {
    flex: 1 1 auto;
    min-height: 0;
    display: flex;
    flex-direction: column;
}

.route-view {
    flex: 1 1 auto;
    min-height: 0;
}

.layout-footer {
    flex-shrink: 0;
}

.header {
    padding: 0;
    width: 100%;
    z-index: 100;
    height: auto;
}

.page {
    height: 100vh;
    background: var(--app-shell-radial), var(--app-shell-bg);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
</style>
