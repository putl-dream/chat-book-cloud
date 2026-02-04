<template>
    <el-container class="page">
        <el-header class="header">
            <CreativeHeader/>
        </el-header>

        <el-container class="main">
            <el-aside class="aside">
                <CreativeAside />
            </el-aside>
            <el-main class="main">
                <router-view/>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import CreativeHeader from "@/components/CreativeHeader.vue";
import CreativeAside from "@/components/CreativeAside.vue";
</script>

<style scoped>

.main {
    padding: 10px;
}

.header {
    padding: 0;
    min-width: 100%;
}

.page {
    min-height: 100vh;
    background-image: linear-gradient(20deg, #ece6ff 25%, #fcfcfc, #ffe5e5);
}
</style>