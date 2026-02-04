<template>
    <el-container class="page">
        <el-header class="header">
            <CommonHeader/>
        </el-header>

        <el-main class="main">
            <router-view/>
        </el-main>
    </el-container>
</template>

<script setup>
import CommonHeader from "@/components/CommonHeader.vue";
</script>

<style scoped>

.main {
    padding: 20px;
    max-width: var(--container-width-lg);
    margin: 0 auto;
    width: 100%;
}

.header {
    padding: 0;
    min-width: 100%;
    position: sticky;
    top: 0;
    z-index: 100;
}

.page {
    min-height: 100vh;
    background: radial-gradient(circle at top right, #f8fafc, #f1f5f9);
    display: flex;
    flex-direction: column;
}
</style>