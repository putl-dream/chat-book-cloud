<template>
    <el-container class="page">
        <el-header class="header">
            <CommonHeader :show-search="false"/>
        </el-header>

        <el-main class="main">
            <router-view/>
        </el-main>
    </el-container>
</template>

<script setup>
import CommonHeader from "@/components/common/CommonHeader.vue";
</script>

<style scoped>
.main{
    padding: 0;
}

.header {
    padding: 0;
    min-width: 100%;
}

.page {
    background: var(--app-shell-radial), var(--app-shell-bg);
    min-height: 100vh;
}
</style>
