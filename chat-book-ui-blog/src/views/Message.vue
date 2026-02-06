<template>
    <div class="message-page">
        <div class="message-container">
            <div class="message-header">
                <h2 class="page-title">
                    <span class="text-gradient">消息通知</span>
                </h2>
                <div class="header-decoration"></div>
            </div>

            <div class="message-list-wrapper">
                <div v-if="loading" class="loading">
                    <el-skeleton :rows="5" animated />
                </div>

                <template v-else-if="messages.length > 0">
                    <transition-group name="list-fade" tag="div" class="message-grid">
                        <div v-for="(message, index) in messages" :key="message.id || index"
                            class="message-card-wrapper" :style="{ '--delay': `${index * 0.1}s` }"
                            @click="openArticle(message.id)">
                            <div class="message-card glass-panel">
                                <div class="message-icon-box">
                                    <el-icon :size="24">
                                        <BellFilled />
                                    </el-icon>
                                </div>
                                <div class="message-content-area">
                                    <MessageCard :message="message" />
                                </div>
                                <div class="card-arrow">
                                    <el-icon>
                                        <ArrowRight />
                                    </el-icon>
                                </div>
                            </div>
                        </div>
                    </transition-group>
                </template>

                <div v-else class="empty-state glass-panel">
                    <el-empty description="暂无新消息" />
                </div>
            </div>
        </div>
    </div>
</template>

<script setup>
import MessageCard from "@/components/widget/MessageCard.vue";
import { getMessage } from "@/api/user.js";
import { onMounted, ref } from "vue";
import router from "@/router/index.js";
import { ElSkeleton, ElEmpty } from 'element-plus';

const messages = ref([]);
const loading = ref(false);

const messageRequest = async () => {
    loading.value = true;
    try {
        const res = await getMessage();
        if (res) {
            messages.value = res;
        }
    } catch (error) {
        console.error("获取消息失败", error);
    } finally {
        loading.value = false;
    }
}

const openArticle = async (id) => {
    await router.push({ name: 'Article', params: { id: id } });
};

onMounted(() => {
    messageRequest();
})

</script>

<style scoped>
.message-page {
    min-height: 100%;
    background-color: var(--bg-color-base);
    padding: 40px 24px;
    background-image:
        radial-gradient(circle at 10% 20%, rgba(var(--color-primary-rgb), 0.05) 0%, transparent 20%),
        radial-gradient(circle at 90% 80%, rgba(var(--color-success-rgb), 0.05) 0%, transparent 20%);
}

.message-container {
    max-width: 900px;
    margin: 0 auto;
}

.message-header {
    margin-bottom: 32px;
    position: relative;
    padding-left: 16px;
}

.header-decoration {
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 4px;
    height: 32px;
    background: linear-gradient(to bottom, var(--color-primary), #a0c4ff);
    border-radius: 4px;
}

.page-title {
    font-size: 28px;
    font-weight: 800;
    margin: 0;
    line-height: 1.2;
}

.text-gradient {
    background: linear-gradient(135deg, var(--text-color-primary) 30%, var(--color-primary) 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
}

.message-grid {
    display: flex;
    flex-direction: column;
    gap: 16px;
}

.message-card-wrapper {
    /* For animation stagger */
    animation: slideUp 0.5s ease backwards;
    animation-delay: var(--delay);
}

@keyframes slideUp {
    from {
        opacity: 0;
        transform: translateY(20px);
    }

    to {
        opacity: 1;
        transform: translateY(0);
    }
}

.message-card {
    display: flex;
    align-items: center;
    padding: 20px;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.8);
    backdrop-filter: blur(12px);
    border: 1px solid rgba(255, 255, 255, 0.6);
    box-shadow: 0 4px 6px rgba(0, 0, 0, 0.02);
    cursor: pointer;
    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.message-card:hover {
    transform: translateY(-4px) scale(1.01);
    box-shadow: 0 12px 24px rgba(0, 0, 0, 0.06);
    background: rgba(255, 255, 255, 0.95);
    border-color: var(--color-primary-light);
}

.message-icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    background: linear-gradient(135deg, #e0eaff 0%, #f0f7ff 100%);
    color: var(--color-primary);
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 20px;
    flex-shrink: 0;
    transition: transform 0.3s ease;
}

.message-card:hover .message-icon-box {
    transform: rotate(10deg);
    background: var(--color-primary);
    color: #fff;
}

.message-content-area {
    flex: 1;
    min-width: 0;
    /* Fix flex child text overflow */
}

.card-arrow {
    margin-left: 16px;
    color: var(--text-color-placeholder);
    transition: transform 0.3s ease;
}

.message-card:hover .card-arrow {
    transform: translateX(4px);
    color: var(--color-primary);
}

.loading {
    padding: 24px;
    background: rgba(255, 255, 255, 0.5);
    border-radius: 16px;
}

.empty-state {
    padding: 60px 0;
    border-radius: 16px;
    background: rgba(255, 255, 255, 0.5);
    backdrop-filter: blur(10px);
    border: 1px solid rgba(255, 255, 255, 0.3);
}

/* Transitions */
.list-fade-enter-active,
.list-fade-leave-active {
    transition: all 0.5s ease;
}

.list-fade-enter-from,
.list-fade-leave-to {
    opacity: 0;
    transform: translateX(-20px);
}
</style>