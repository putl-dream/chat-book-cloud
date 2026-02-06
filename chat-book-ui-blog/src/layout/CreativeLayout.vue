<template>
    <el-container class="page">
        <!-- Global Background Decoration -->
        <div class="bg-decoration-1"></div>
        <div class="bg-decoration-2"></div>

        <el-header class="header">
            <CreativeHeader/>
        </el-header>

        <el-container class="main-container">
            <el-aside class="aside" width="240px">
                <CreativeAside />
            </el-aside>
            <el-main class="main-content">
                <router-view v-slot="{ Component }">
                    <transition name="fade-slide" mode="out-in">
                        <component :is="Component" />
                    </transition>
                </router-view>
            </el-main>
        </el-container>
    </el-container>
</template>

<script setup>
import CreativeHeader from "@/components/CreativeHeader.vue";
import CreativeAside from "@/components/CreativeAside.vue";
</script>

<style scoped>
.page {
    min-height: 100vh;
    background-color: #f8fafc;
    position: relative;
    overflow: hidden;
}

/* Ambient Background Decorations */
.bg-decoration-1 {
    position: absolute;
    top: -10%;
    left: -10%;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(37, 99, 235, 0.15) 0%, rgba(255, 255, 255, 0) 70%);
    border-radius: 50%;
    filter: blur(40px);
    z-index: 0;
    pointer-events: none;
}

.bg-decoration-2 {
    position: absolute;
    bottom: -10%;
    right: -5%;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(16, 185, 129, 0.1) 0%, rgba(255, 255, 255, 0) 70%);
    border-radius: 50%;
    filter: blur(40px);
    z-index: 0;
    pointer-events: none;
}

.header {
    padding: 0;
    min-width: 100%;
    position: relative;
    z-index: 20;
}

.main-container {
    position: relative;
    z-index: 10;
    max-width: 1440px;
    margin: 0 auto;
    width: 100%;
}

.aside {
    padding-top: 20px;
    overflow: visible; /* Allow menu shadows to show */
}

.main-content {
    padding: 20px 40px;
}

/* Router View Transition */
.fade-slide-enter-active,
.fade-slide-leave-active {
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.fade-slide-enter-from {
    opacity: 0;
    transform: translateY(10px);
}

.fade-slide-leave-to {
    opacity: 0;
    transform: translateY(-10px);
}
</style>