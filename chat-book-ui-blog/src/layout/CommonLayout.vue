<template>
    <el-container class="page">
        <el-header class="header">
            <CommonHeader/>
        </el-header>

        <el-main class="main" id="common-layout-main">
            <router-view/>
        </el-main>
    </el-container>
</template>

<script setup>
import CommonHeader from "@/components/CommonHeader.vue";
</script>

<style scoped>

.main {
    padding: 0;
    width: 100%;
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;
}

.header {
    padding: 0;
    width: 100%;
    z-index: 100;
    height: auto;
}

.page {
    height: 100vh;
    background: radial-gradient(circle at top right, #f8fafc, #f1f5f9);
    display: flex;
    flex-direction: column;
    overflow: hidden;
}
</style>