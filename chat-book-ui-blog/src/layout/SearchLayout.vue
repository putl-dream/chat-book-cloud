<template>
    <el-container class="page">
        <el-header class="header">
            <CommonHeader :show-search="false"/>
        </el-header>

        <el-main class="main">
            <div class="main-view">
                <router-view/>
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
.main{
    padding: 0;
    flex: 1;
    display: flex;
    flex-direction: column;
}

.main-view {
    flex: 1 0 auto;
}

.layout-footer {
    flex-shrink: 0;
}

.header {
    padding: 0;
    min-width: 100%;
}

.page {
    background: var(--app-shell-radial), var(--app-shell-bg);
    min-height: 100vh;
    display: flex;
    flex-direction: column;
}
</style>
